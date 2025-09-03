package com.wepong.pongdang.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.wepong.pongdang.dto.response.QuizResponseDTO;
import com.wepong.pongdang.entity.QuizEntity;
import com.wepong.pongdang.genai.QuizPrompts;
import com.wepong.pongdang.genai.QuizSimilarityUtil;
import com.wepong.pongdang.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final Client client;                       // GeminiConfig에서 만든 @Bean
    private final GenerateContentConfig defaultGenConfig; // GeminiConfig의 defaultGenConfig @Bean
    private final ObjectMapper objectMapper;           // Spring Boot 기본 제공
    private final QuizRepository quizRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 모델명은 바로 주입 (PostConstruct 불필요)
    @Value("${gemini.model}")
    private String model;


    @Transactional
    public void generateTodayAndSave() {
        // 1) 프롬프트로 퀴즈 생성 요청
        Content user = Content.builder()
                .role("user")
                .parts(List.of(Part.fromText(QuizPrompts.RANDOM_QUIZ_JSON)))
                .build();

        // 2) JSON만 받도록
        GenerateContentResponse res = client.models.generateContent(
                model,
                List.of(user),
                defaultGenConfig
        );

        String json = res.text(); // 모델이 낸 JSON 전체

        // 3) 파싱 + 검증(필수 필드/개수/인덱스)
        QuizResponseDTO.GenerateResponse body;
        try {
            if (json.trim().startsWith("[")) {
                json = "{ \"questions\": " + json + " }";
            }
            body = objectMapper.readValue(json, QuizResponseDTO.GenerateResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("퀴즈 JSON 파싱 실패: " + e.getMessage(), e);
        }
        if (body.getQuestions() == null || body.getQuestions().size() != 3) {
            throw new IllegalStateException("퀴즈는 정확히 3문제여야 합니다.");
        }
        for (QuizResponseDTO.GeneratedQuestion q : body.getQuestions()) {
            if (q.getPosition() == null || q.getPosition() < 1 || q.getPosition() > 3)
                throw new IllegalStateException("position은 1~3이어야 합니다.");
            if (q.getAnswerIdx() == null || q.getAnswerIdx() < 0 || q.getAnswerIdx() > 3)
                throw new IllegalStateException("answer_idx는 0~3이어야 합니다.");
        }

        // 4) 저장: 오늘 데이터가 있는 경우 아무것도 X
        LocalDate today = LocalDate.now(KST);

        boolean exists = quizRepository.existsByQuizDate(today);
        if (exists) {
            // 오늘 퀴즈 이미 있음 → 아무것도 하지 않음
            System.out.println("아무것도 안 함");
            return;
        }

        // 여기서는 단순히 그대로 저장
        for (QuizResponseDTO.GeneratedQuestion gq : body.getQuestions()) {
            QuizEntity entity = QuizEntity.builder()
                    .quizDate(today)
                    .position(gq.getPosition())
                    .question(gq.getQuestion())
                    .choice1(gq.getChoice1())
                    .choice2(gq.getChoice2())
                    .choice3(gq.getChoice3())
                    .choice4(gq.getChoice4())
                    .answerIdx(gq.getAnswerIdx())
                    .explanation(gq.getExplanation())
                    .build();
            quizRepository.save(entity);
        }
    }

    @Transactional
    public void regenerateDuplicates() {
        LocalDate today = LocalDate.now(KST);
        List<QuizEntity> todayQuizzes = quizRepository.findByQuizDateOrderByPosition(today); // 오늘 퀴즈
        List<QuizEntity> pastQuizzes = quizRepository.findByQuizDateBefore(today);          // 과거 모든 퀴즈

        for (QuizEntity quiz : todayQuizzes) {
            boolean duplicate = true;
            int retry = 0;

            do { // 최대 3번까지 재시도
                duplicate = pastQuizzes.stream().anyMatch(past ->
                        quiz.getQuestion().equals(past.getQuestion())
                                || QuizSimilarityUtil.isDuplicate(quiz.getQuestion(), past.getQuestion())
                                || QuizSimilarityUtil.isDuplicate(
                                quiz.getChoice1() + " " + quiz.getChoice2() + " " +
                                        quiz.getChoice3() + " " + quiz.getChoice4(),
                                past.getChoice1() + " " + past.getChoice2() + " " +
                                        past.getChoice3() + " " + past.getChoice4()
                        )
                );

                if (duplicate) {
                    retry++;

                    List<String> bannedQuestions = quizRepository.findByQuizDateBefore(today)
                            .stream()
                            .map(QuizEntity::getQuestion)
                            .toList();

                    // === AI 재생성 요청 ===
                    String prompt = String.format(
                            QuizPrompts.RERANDOM_QUIZ_JSON_TEMPLATE,
                            quiz.getPosition(), bannedQuestions
                    );

                    Content user = Content.builder()
                            .role("user")
                            .parts(List.of(Part.fromText(prompt)))
                            .build();
                    GenerateContentResponse res = client.models.generateContent(
                            model,
                            List.of(user),
                            defaultGenConfig
                    );

                    String json = res.text();
                    QuizResponseDTO.GenerateResponse body;
                    try {
                        if (json.trim().startsWith("[")) {
                            json = "{ \"questions\": " + json + " }";
                        }
                        body = objectMapper.readValue(json, QuizResponseDTO.GenerateResponse.class);
                    } catch (Exception e) {
                        throw new IllegalStateException("퀴즈 재생성 JSON 파싱 실패: " + e.getMessage(), e);
                    }

                    if (body.getQuestions() == null || body.getQuestions().isEmpty()) {
                        throw new IllegalStateException("퀴즈 재생성이 실패했습니다.");
                    }

                    QuizResponseDTO.GeneratedQuestion gq = body.getQuestions().get(0);

                    // === 새 데이터로 덮어쓰기 ===
                    quiz.setQuestion(gq.getQuestion());
                    quiz.setChoice1(gq.getChoice1());
                    quiz.setChoice2(gq.getChoice2());
                    quiz.setChoice3(gq.getChoice3());
                    quiz.setChoice4(gq.getChoice4());
                    quiz.setAnswerIdx(gq.getAnswerIdx());
                    quiz.setExplanation(gq.getExplanation());

                    quizRepository.save(quiz);
                }
            } while (duplicate && retry < 3);

            if (retry >= 3 && duplicate) {
                log.error("❌ position={} 퀴즈가 3회 재시도 후에도 중복 발생", quiz.getPosition());
            }
        }
    }


    /** 오늘자 퀴즈 조회 */
    public List<QuizResponseDTO.QuizView> getToday() {
        LocalDate today = LocalDate.now(KST);

        return quizRepository.findByQuizDateOrderByPosition(today)
                .stream()
                .map(QuizEntity::toDto)
                .toList();
    }

    public List<QuizResponseDTO.QuizView> getTodayWithAutoGenerate() {
        LocalDate today = LocalDate.now(KST);
        List<QuizEntity> list = quizRepository.findByQuizDateOrderByPosition(today);

        // 오늘 퀴즈 없으면 생성 후 재조회
        if (list.isEmpty()) {
            generateTodayAndSave();
            regenerateDuplicates();
            list = quizRepository.findByQuizDateOrderByPosition(today);
        }

        // Entity → DTO 변환
        return list.stream()
                .map(QuizEntity::toDto)
                .toList();
    }
}

