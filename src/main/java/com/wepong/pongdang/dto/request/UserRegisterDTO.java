package com.wepong.pongdang.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {
	private String userName;
	private String password;
	private String passwordCheck;
	private String nickname;
	private String email;
	private String birthDate;
	private String phoneNumber;
	private boolean agreePrivacy;
    private boolean linkedWithBetting;
}