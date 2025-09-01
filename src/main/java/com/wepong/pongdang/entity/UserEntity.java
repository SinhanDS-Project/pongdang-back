package com.wepong.pongdang.entity;

import com.wepong.pongdang.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Date;

@Entity(name = "user") // 테이블 이름
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Id // PK 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String userName;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String password;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String nickname;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)", unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)", unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private Date birthDate;

    @ColumnDefault("0")
    private boolean agreePrivacy;

    @Column(columnDefinition = "VARCHAR(225) DEFAULT ''")
    private String profileImage;

    @ColumnDefault("0")
    private Boolean tutorialCheck;

    //  AuthToken 관계 (1:1)
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private AuthTokenEntity token;

    // Wallet 관계 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<WalletEntity> wallets = new java.util.ArrayList<>();

    public void updatePassword(String password) {
        this.password = password;
    }
}
