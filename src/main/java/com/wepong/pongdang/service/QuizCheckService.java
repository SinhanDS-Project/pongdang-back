package com.wepong.pongdang.service;


import com.wepong.pongdang.dto.request.QuizRequestDTO;
import com.wepong.pongdang.dto.response.QuizResponseDTO;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.QuizCheckEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.QuizAlreadyTakenException;
import com.wepong.pongdang.exception.UserCannotFoundException;
import com.wepong.pongdang.repository.QuizCheckRepository;
import com.wepong.pongdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizCheckService {

    private final UserRepository userRepository;
    private final QuizCheckRepository quizCheckRepository;
    private final HistoryService historyService;
    private final WalletService walletService;

    @Transactional
    public QuizResponseDTO.QuizCheckResponse markTodayQuizTaken(Long userId) { // check
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserCannotFoundException());

        LocalDate today = LocalDate.now();

        // 유저의 퀴즈 기록 하나만 조회
        Optional<QuizCheckEntity> optionalCheck = quizCheckRepository.findByUser(user);

        if (optionalCheck.isPresent()) {
            QuizCheckEntity check = optionalCheck.get();

            // 오늘 이미 기록이 있다면 → 예외
            if (check.getQuizDate().isEqual(today)) {
                throw new QuizAlreadyTakenException();
            }

            // 과거 기록이 있다면 → 오늘 날짜로 update
            check.setQuizDate(today);
            check.setTaken(true);
            quizCheckRepository.save(check);
        } else {
            // 기록 자체가 없으면 → 새 insert
            QuizCheckEntity newCheck = QuizCheckEntity.builder()
                    .user(user)
                    .quizDate(today)
                    .taken(true)
                    .build();
            quizCheckRepository.save(newCheck);
        }


        return QuizResponseDTO.QuizCheckResponse.builder()
                .message("오늘 퀴즈 참여가 기록되었습니다.")
                .build();
    }

    @Transactional
    public QuizResponseDTO.QuizSubmitResponse submitQuiz(
            Long userId, QuizRequestDTO request
    ) {
        int reward = Math.min(
                request.getCorrectCount() != null ? request.getCorrectCount() : 0,
                3
        ); // 최대 3 포인트 제한

        if (reward > 0) {
            PongHistoryEntity history = PongHistoryEntity.builder()
                    .type(PongHistoryType.ADD)
                    .amount(reward)
                    .build();

            historyService.insertPointHistory(history, userId);
            walletService.add(reward, userId, WalletType.PONG);
        }

        return QuizResponseDTO.QuizSubmitResponse.builder()
                .message("퀴즈 종료!")
                .reward(reward)
                .build();
    }
}
