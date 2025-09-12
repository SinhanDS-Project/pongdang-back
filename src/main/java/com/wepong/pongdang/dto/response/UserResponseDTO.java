package com.wepong.pongdang.dto.response;

import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.WalletEntity;
import com.wepong.pongdang.entity.enums.Role;
import com.wepong.pongdang.model.aws.S3ImagePathDeserializer;
import com.wepong.pongdang.model.aws.S3ImageUrlSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String userName;
    private String nickname;
    private String email;
    private Date birthDate;
    private String phoneNumber;
    @JsonSerialize(using = S3ImageUrlSerializer.class)
    @JsonDeserialize(using = S3ImagePathDeserializer.class)
    private String profileImg;
    private Long pongBalance;
    private Long donaBalance;
    private Boolean linkedWithBetting;
    private Boolean tutorialCheck;
    private LocalDateTime updatedAt;
    private Role role;

    public static UserResponseDTO from(UserEntity userEntity, String profileImg, WalletEntity pong, WalletEntity dona) {
        return UserResponseDTO.builder()
                .id(userEntity.getId())
                .nickname(userEntity.getNickname())
                .userName(userEntity.getUserName())
                .birthDate(userEntity.getBirthDate())
                .email(userEntity.getEmail())
                .profileImg(profileImg)
                .phoneNumber(userEntity.getPhoneNumber())
                .pongBalance(pong.getPongBalance())
                .donaBalance(dona.getPongBalance())
                .linkedWithBetting(userEntity.getLinkedWithBetting())
                .tutorialCheck(userEntity.getTutorialCheck())
                .updatedAt(userEntity.getUpdatedAt())
                .role(userEntity.getRole())
                .build();
    }
}
