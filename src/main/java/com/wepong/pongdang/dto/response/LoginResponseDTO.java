package com.wepong.pongdang.dto.response;

import com.wepong.pongdang.entity.mapping.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String message;
    private UserInfo user;
}