package com.wepong.pongdang.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDTO {
	private String password; // 현재 비밀번호
	private String newPassword; // 변경할 비밀번호
	private String newNickname; // 변경할 닉네임
}
