package com.wepong.pongdang.dto.request;

import com.wepong.pongdang.model.aws.S3ImagePathDeserializer;
import com.wepong.pongdang.model.aws.S3ImageUrlSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDTO {
	private String password; // 현재 비밀번호
	private String newPassword; // 변경할 비밀번호
	private String newNickname; // 변경할 닉네임
    private MultipartFile profileImage; // 새로 업로드할 이미지
}
