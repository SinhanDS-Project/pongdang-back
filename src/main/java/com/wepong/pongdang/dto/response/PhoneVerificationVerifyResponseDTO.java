package com.wepong.pongdang.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhoneVerificationVerifyResponseDTO {
    private String phone;     // 인증 대상 전화번호
    private boolean verified; // 인증 성공 여부
    private String message;   // 응답 메시지
}
