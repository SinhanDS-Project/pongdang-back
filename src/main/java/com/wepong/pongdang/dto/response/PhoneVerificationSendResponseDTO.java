package com.wepong.pongdang.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhoneVerificationSendResponseDTO {
    private String phone;     // 요청된 전화번호
    private boolean success;  // 발송 성공 여부
    private String message;   // 응답 메시지
}
