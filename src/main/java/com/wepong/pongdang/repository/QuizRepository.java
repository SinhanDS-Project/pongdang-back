package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {

    List<QuizEntity> findByQuizDateOrderByPosition(LocalDate quizDate);

    boolean existsByQuizDate(LocalDate quizDate);

    List<QuizEntity> findByQuizDateBefore(LocalDate today);
}
