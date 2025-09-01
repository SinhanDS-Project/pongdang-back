package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.PhoneVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneVerificationRepository extends JpaRepository<PhoneVerificationEntity, String> {

    // 특정 전화번호로 조회
    PhoneVerificationEntity findByPhoneNumber(String phoneNumber);

    // 특정 전화번호 + 인증번호로 조회 (검증할 때 유용)
    PhoneVerificationEntity findByPhoneNumberAndVerificationCode(String phoneNumber, String verificationCode);
    
    //삭제
    void deleteByPhoneNumber(String phoneNumber);
}
