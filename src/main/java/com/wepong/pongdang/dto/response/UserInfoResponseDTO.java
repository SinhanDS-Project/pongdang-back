package com.wepong.pongdang.dto.response;

import com.wepong.pongdang.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDTO {
    private Long id;
    private String userName;
    private String nickname;
    private String email;
    private String phoneNumber;
    private Date birthDate;
    private String profileImage;
    private Boolean tutorialCheck;
    private Boolean linkedWithBetting;
    private LocalDateTime updatedAt;

    // 엔티티 → DTO 변환
    public static UserInfoResponseDTO from(UserEntity entity) {
        return new UserInfoResponseDTO(
                entity.getId(),
                entity.getUserName(),
                entity.getNickname(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getBirthDate(),
                entity.getProfileImage(),
                entity.getTutorialCheck(),
                entity.getLinkedWithBetting(),
                entity.getUpdatedAt()
        );
    }
}