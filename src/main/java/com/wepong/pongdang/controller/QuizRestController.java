package com.wepong.pongdang.controller;


import com.wepong.pongdang.dto.response.QuizResponseDTO;
import com.wepong.pongdang.entity.QuizEntity;
import com.wepong.pongdang.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quizzes")
public class QuizRestController {

    private final QuizService quizService;

    // (관리) 오늘 세트 생성/갱신
    @PostMapping("/generate-quiz")
    public ResponseEntity<List<QuizResponseDTO.QuizView>> generateToday() {
        // 1) 오늘 문제 생성 & 저장
        quizService.generateTodayAndSave();

        // 2) 중복된 문제 있으면 재생성 → DB update
        quizService.regenerateDuplicates();

        // 3) 최종 확정된 오늘 문제 다시 읽어오기
        List<QuizEntity> saved = quizService.getToday();

        // 4) DTO 변환해서 반환
        return ResponseEntity.ok(
                saved.stream()
                        .map(q -> QuizResponseDTO.QuizView.builder()
                                .position(q.getPosition())
                                .question(q.getQuestion())
                                .choice1(q.getChoice1())
                                .choice2(q.getChoice2())
                                .choice3(q.getChoice3())
                                .choice4(q.getChoice4())
                                .answerIdx(q.getAnswerIdx())
                                .explanation(q.getExplanation())
                                .build())
                        .toList()
        );
    }

    // 오늘 세트 조회(사용자용)
    @GetMapping("/today-quiz")
    public ResponseEntity<List<QuizResponseDTO.QuizView>> today() {
        List<QuizEntity> list = quizService.getToday();
        return ResponseEntity.ok(
                list.stream()
                        .map(q -> QuizResponseDTO.QuizView.builder()
                                .position(q.getPosition())
                                .question(q.getQuestion())
                                .choice1(q.getChoice1())
                                .choice2(q.getChoice2())
                                .choice3(q.getChoice3())
                                .choice4(q.getChoice4())
                                .answerIdx(q.getAnswerIdx())
                                .explanation(q.getExplanation())
                                .build())
                        .toList()
        );
    }
}

