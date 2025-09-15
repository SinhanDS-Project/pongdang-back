package com.wepong.pongdang.model.multi.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomStateDTO {
    private int currentTurn; // 플레이어 차례
    private int round; // 현재 라운드
    private int maxRound;
    private int pot; // 금고 보유금
    private int doubleCount;
    private boolean isDouble;
}
