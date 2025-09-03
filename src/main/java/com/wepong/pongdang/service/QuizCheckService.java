package com.wepong.pongdang.service;


import com.wepong.pongdang.entity.QuizCheckEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.exception.QuizAlreadyTakenException;
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

    @Transactional
    public void markTodayQuizTaken(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();

        // 1) 해당 유저의 퀴즈 체크 기록 조회
        Optional<QuizCheckEntity> optionalCheck = quizCheckRepository.findByUser(user);

        if (optionalCheck.isPresent()) {
            QuizCheckEntity check = optionalCheck.get();

            // 오늘 이미 기록이 있다면 → 예외
            if (check.getQuizDate().isEqual(today)) {
                throw new QuizAlreadyTakenException(); // 오늘 퀴즈는 이미 참여 완료되었습니다
            }

            // 과거 기록이 있다면 → 오늘 날짜로 update
            check.setQuizDate(today);
            check.setTaken(true);

            quizCheckRepository.save(check);
        } else {
            // 기록 자체가 없으면 → insert
            QuizCheckEntity newCheck = QuizCheckEntity.builder()
                    .user(user)
                    .quizDate(today)
                    .taken(true)
                    .build();

            quizCheckRepository.save(newCheck);
        }
    }

}
