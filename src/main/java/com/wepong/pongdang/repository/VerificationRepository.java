package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.EmailVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationRepository extends JpaRepository<EmailVerificationEntity, String> {
    void deleteByEmail(String email);
}
