package com.wepong.pongdang.entity;

import com.wepong.pongdang.entity.enums.Level;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "game_level")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class GameLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Level level;

    @Column(nullable = false)
    private double probability;

    @Column(nullable = false)
    private int entryFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private GameEntity game;

    // RewardPerResult (1:N)
    @OneToMany(mappedBy = "gameLevel", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RewardPerResultEntity> rewards = new ArrayList<>();

    // GameRoom (1:N)
    @OneToMany(mappedBy = "gameLevel", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<GameRoomEntity> rooms = new ArrayList<>();
}
