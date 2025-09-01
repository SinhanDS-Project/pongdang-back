package com.wepong.pongdang.entity;

import com.wepong.pongdang.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "quiz_check")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QuizCheckEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 간단히 userId만 저장 (연관관계 매핑이 필요하면 @ManyToOne으로 변경)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quiz_date", nullable = false)
    private LocalDate quizDate;

    @Column(nullable = false)
    private boolean taken = false;            // 제출 여부
}
