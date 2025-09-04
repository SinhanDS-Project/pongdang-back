package com.wepong.pongdang.entity.mapping;

import com.wepong.pongdang.entity.BoardEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "reply")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReplyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private BoardEntity board;
}
