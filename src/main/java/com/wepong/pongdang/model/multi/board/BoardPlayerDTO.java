package com.wepong.pongdang.model.multi.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardPlayerDTO {
    private Long userId;
    private String nickname;
    private Long roomId;
    private boolean isReady;
    private String turtleId; // color 이름으로 하기

    private int balance; //
    private int position; // 현재 위치
    private int turnOrder; // 차례(1~4)
    private boolean skipTurn; // 무인도 여부
    private boolean active; // false: 파산
    private int rank; // 순위
    private int reward; // 적립 퐁
}
