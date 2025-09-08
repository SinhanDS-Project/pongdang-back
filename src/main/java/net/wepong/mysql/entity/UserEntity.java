package net.wepong.mysql.entity;

import com.wepong.pongdang.entity.common.BaseEntity;
import com.wepong.pongdang.entity.mapping.DonationEntity;
import com.wepong.pongdang.entity.mapping.PurchaseEntity;
import com.wepong.pongdang.entity.mapping.ReplyEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity(name = "BettingUser")   // JPQL에서 사용할 엔티티 이름
@Table(name = "`user`")         // 테이블 이름
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    @Id // PK 설정
    @Column(nullable = false, columnDefinition = "CHAR(32)")
    private String uid;

    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private String userName;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String password;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String nickname;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)", unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "VARCHAR(30)", unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private Date birthDate;

    @ColumnDefault("0")
    private boolean agreePrivacy;

    @Column(columnDefinition = "VARCHAR(225) DEFAULT ''")
    private String profileImg;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "role", length = 50)
    private String role = "USER";

    @Column(name = "point_balance")
    private Integer pointBalance = 0;

}
