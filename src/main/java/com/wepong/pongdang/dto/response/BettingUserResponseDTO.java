package com.wepong.pongdang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class BettingUserResponseDTO {
    private String uid;
    private String userName;
    private String nickname;
    private String email;
    private String phoneNumber;
    private Date birthDate;
    private boolean agreePrivacy;
    private String profileImg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private String role;
    private Integer pointBalance;
}
