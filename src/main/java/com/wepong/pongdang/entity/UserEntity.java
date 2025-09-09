package com.wepong.pongdang.entity;

import com.wepong.pongdang.entity.common.BaseEntity;
import com.wepong.pongdang.entity.enums.Role;
import com.wepong.pongdang.entity.mapping.DonationEntity;
import com.wepong.pongdang.entity.mapping.PurchaseEntity;
import com.wepong.pongdang.entity.mapping.ReplyEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

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

    @ColumnDefault("0")
    private Boolean linkedWithBetting;

    @ColumnDefault("'USER'")
    @Enumerated(EnumType.STRING)
    private Role role;

    //  AuthToken 관계 (1:1)
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private AuthTokenEntity token;

    // Wallet 관계 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<WalletEntity> wallets = new ArrayList<>();

    // 포인트 기록 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PongHistoryEntity> pongHistories = new ArrayList<>();

    // 게임 이력 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<GameHistoryEntity> gameHistories = new ArrayList<>();

    // 구매 이력 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PurchaseEntity> purchases = new ArrayList<>();

    // 기부 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<DonationEntity> donations = new ArrayList<>();

    // 퀴즈 기록 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<QuizCheckEntity> quizChecks = new ArrayList<>();

    // 게시글 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoardEntity> boards = new ArrayList<>();

    // 댓글 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ReplyEntity> replies = new ArrayList<>();

    // 챗봇 로그 (1:N)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChatLogsEntity> chatLogs = new ArrayList<>();

    public void updatePassword(String password) {
        this.password = password;
    }
}
