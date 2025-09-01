package com.wepong.pongdang.service;

import com.wepong.pongdang.config.CoolSmsConfig;
import com.wepong.pongdang.dto.request.PhoneVerificationSendRequestDTO;
import com.wepong.pongdang.dto.request.PhoneVerificationVerifyRequestDTO;
import com.wepong.pongdang.dto.response.PhoneVerificationSendResponseDTO;
import com.wepong.pongdang.dto.response.PhoneVerificationVerifyResponseDTO;
import com.wepong.pongdang.entity.PhoneVerificationEntity;
import com.wepong.pongdang.repository.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final CoolSmsConfig coolSmsConfig;
    private final PhoneVerificationRepository phoneVerificationRepository;

    
    //인증번호 발송
    @Transactional
    public PhoneVerificationSendResponseDTO sendVerificationCode(PhoneVerificationSendRequestDTO requestDTO) {
        // 인증번호 생성 (범위 000000 ~ 999999)
        String code = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(3);



        try {
            // CoolSMS API 초기화
            var messageService = NurigoApp.INSTANCE.initialize(
                    coolSmsConfig.getKey(),
                    coolSmsConfig.getSecret(),
                    "https://api.coolsms.co.kr"
            );

            // 문자 메시지 생성
            Message message = new Message();
            message.setFrom(coolSmsConfig.getFrom());
            message.setTo(requestDTO.getPhone());
            message.setText("[퐁당퐁당] 인증번호: " + code);

            // 문자 발송
            messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("문자 발송 성공");

            // DB 저장 (있으면 덮어쓰기, 없으면 insert)
            PhoneVerificationEntity entity = PhoneVerificationEntity.builder()
                    .phoneNumber(requestDTO.getPhone())
                    .verificationCode(code)
                    .expiredAt(Timestamp.valueOf(expiredAt))
                    .isVerified(false)
                    .build();

            phoneVerificationRepository.save(entity);

            return PhoneVerificationSendResponseDTO.builder()
                    .phone(requestDTO.getPhone())
                    .success(true)
                    .message("인증번호 전송 완료")
                    .build();
        } catch (Exception e) {
            //  CoolSMS API 에러 로그 찍기
            log.error("CoolSMS ERROR: {}", e.getMessage(), e);

            return PhoneVerificationSendResponseDTO.builder()
                    .phone(requestDTO.getPhone())
                    .success(false)
                    .message("문자 전송 실패: " + e.getMessage())
                    .build();
        }
    }

    // 인증번호 확인
    @Transactional
    public PhoneVerificationVerifyResponseDTO verifyCode(PhoneVerificationVerifyRequestDTO requestDTO) {
        PhoneVerificationEntity entity = phoneVerificationRepository.findByPhoneNumber(requestDTO.getPhone());

        if (entity == null) {
            return PhoneVerificationVerifyResponseDTO.builder()
                    .phone(requestDTO.getPhone())
                    .verified(false)
                    .message("해당 번호로 발송된 인증번호가 없습니다.")
                    .build();
        }

        // 만료 여부 확인
        if (entity.getExpiredAt().toLocalDateTime().isBefore(LocalDateTime.now())) {
            return PhoneVerificationVerifyResponseDTO.builder()
                    .phone(requestDTO.getPhone())
                    .verified(false)
                    .message("인증번호가 만료되었습니다.")
                    .build();
        }

        // 코드 일치 여부 확인
        if (!entity.getVerificationCode().equals(requestDTO.getCode())) {
            return PhoneVerificationVerifyResponseDTO.builder()
                    .phone(requestDTO.getPhone())
                    .verified(false)
                    .message("인증번호가 일치하지 않습니다.")
                    .build();
        }

        // 성공  isVerified = true 업데이트
        entity.markVerified();
        phoneVerificationRepository.save(entity);

        return PhoneVerificationVerifyResponseDTO.builder()
                .phone(requestDTO.getPhone())
                .verified(true)
                .message("인증 성공")
                .build();
    }
}
