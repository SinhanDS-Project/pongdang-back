package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.PhoneVerificationSendRequestDTO;
import com.wepong.pongdang.dto.request.PhoneVerificationVerifyRequestDTO;
import com.wepong.pongdang.dto.response.PhoneVerificationSendResponseDTO;
import com.wepong.pongdang.dto.response.PhoneVerificationVerifyResponseDTO;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/phone")
public class PhoneVerificationRestController {

    private final PhoneVerificationService phoneVerificationService;
    private final AuthService authService; //  사용자 중복 체크용


    //  휴대폰 인증 인증번호 발송
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationCode(
            @RequestBody @Valid PhoneVerificationSendRequestDTO requestDTO) {

        // 핸드폰번호 중복체크
        if (authService.isPhoneNumberExists(requestDTO.getPhone())) {
            return ResponseEntity
                    .badRequest()
                    .body("이미 가입된 사용자입니다.");
        }

        // 인증번호 발송(중복아닐때만)
        PhoneVerificationSendResponseDTO responseDTO =
                phoneVerificationService.sendVerificationCode(requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    // 인증번호 확인
    @PostMapping("/verify")
    public ResponseEntity<PhoneVerificationVerifyResponseDTO> verifyCode(
            @RequestBody @Valid PhoneVerificationVerifyRequestDTO requestDTO) {

        PhoneVerificationVerifyResponseDTO responseDTO =
                phoneVerificationService.verifyCode(requestDTO);

        return ResponseEntity.ok(responseDTO);
    }
}
