package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.PhoneVerificationSendRequestDTO;
import com.wepong.pongdang.dto.response.PhoneVerificationSendResponseDTO;
import com.wepong.pongdang.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/phone")
public class PhoneVerificationRestController {

    private final PhoneVerificationService phoneVerificationService;

    /**
     * 휴대폰 인증번호 발송
     */
    @PostMapping("/send")
    public PhoneVerificationSendResponseDTO sendVerificationCode(
            @RequestBody @Valid PhoneVerificationSendRequestDTO requestDTO) {
        return phoneVerificationService.sendVerificationCode(requestDTO);
    }
}
