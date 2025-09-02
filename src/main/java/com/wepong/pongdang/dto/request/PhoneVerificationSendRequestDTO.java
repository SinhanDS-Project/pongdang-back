package com.wepong.pongdang.dto.request;

import lombok.Getter;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PhoneVerificationSendRequestDTO {
    @NotBlank
    private String phone;
}