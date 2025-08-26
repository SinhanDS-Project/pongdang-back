package com.wepong.pongdang.dto.request;

import lombok.Getter;
import jakarta.validation.constraints.NotBlank;

@Getter
public class PhoneVerificationRequestDTO {
    @NotBlank private String phone;
}