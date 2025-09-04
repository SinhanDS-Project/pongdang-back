package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.QuizCheckEntity;
import com.wepong.pongdang.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface QuizCheckRepository extends JpaRepository<QuizCheckEntity, Long> {

    Optional<QuizCheckEntity> findByUser(UserEntity user);

    Optional<QuizCheckEntity> findByUserId(Long userId);

}
