package com.wepong.pongdang.entity;

import com.wepong.pongdang.entity.common.BaseEntity;
import com.wepong.pongdang.entity.enums.GameStatus;
import com.wepong.pongdang.entity.enums.GameType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "game")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "VARCHAR(225)")
    private String name;

    @Enumerated(EnumType.STRING)
    private GameType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "VARCHAR(225) DEFAULT ''")
    private String gameImage;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    //  GameLevel (1:N)
    @OneToMany(mappedBy = "game", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<GameLevelEntity> levels = new ArrayList<>();

    //  GameHistory (1:N)
    @OneToMany(mappedBy = "game", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<GameHistoryEntity> histories = new ArrayList<>();
}
