package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.QuizCheckEntity;
import com.wepong.pongdang.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuizCheckRepository extends JpaRepository<QuizCheckEntity, Long> {

    Optional<QuizCheckEntity> findByUserAndQuizDate(UserEntity user, LocalDate quizDate);

    Optional<QuizCheckEntity> findByUser(UserEntity user);
}
