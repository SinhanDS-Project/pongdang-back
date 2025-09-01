package com.wepong.pongdang.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.sql.Timestamp;

@Entity(name = "email_verification")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmailVerificationEntity {

    @Id
    @Column(columnDefinition = "VARCHAR(100)")
    private String email;

    @Column(columnDefinition = "VARCHAR(6)")
    private String verificationCode;

    @Column(nullable = false)
    private Timestamp expiredAt;

    @Column(nullable = false)
    private boolean isVerified;


    public void markVerified() {
        this.isVerified = true;
    }
}
