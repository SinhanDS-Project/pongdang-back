package com.wepong.pongdang.dto.request;

import lombok.Getter;
import jakarta.validation.constraints.NotBlank;

@Getter
public class PhoneVerificationVerifyRequestDTO {
    @NotBlank
    private String phone; // 전화번호

    @NotBlank
    private String code;  // 사용자가 입력한 인증번호
}
