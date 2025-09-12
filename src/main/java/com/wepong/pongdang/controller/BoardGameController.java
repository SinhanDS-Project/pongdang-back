package com.wepong.pongdang.controller;

import com.wepong.pongdang.model.multi.board.*;
import com.wepong.pongdang.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BoardGameController {

    private final BoardPlayerService boardPlayerService;
    private final WebSocketService webSocketService;
    private final BoardGameService boardGameService;
    private final LandService landService;
    private final RoomStateService roomStateService;

    // 게임 시작
    @MessageMapping("/board/start/{roomId}")
    public void startGame(@DestinationVariable Long roomId, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        // 랜덤 거북이 세팅
        boardPlayerService.setRandomTurtle(userId, roomId);

        // RoomState, Land 메모리 생성 후 전송
        boardGameService.startGame(roomId, gameType);
    }

    // 주사위 굴리기 -> dice 값만큼 포지션 +
    @MessageMapping("/roll/{roomId}")
    public void handleRoll(@DestinationVariable Long roomId,
                             @Payload Map<String, Integer> payload,
                             SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int dice = payload.get("dice");
        boardGameService.roll(roomId, userId, dice);

        webSocketService.sendGame(roomId, "roll", gameType, boardPlayerService.getPlayers(roomId));
    }

    // 땅 구매
    @MessageMapping("/purchase/{roomId}")
    public void handlePurchase(@DestinationVariable Long roomId,
                          @Payload Map<String, Integer> payload,
                          SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int landId = payload.get("landId");
        boardGameService.purchase(roomId, userId, landId);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("land", landService.getLand(roomId, landId));

        webSocketService.sendGame(roomId, "pay", gameType, data);
    }

    // 통행료 지불+파산처리(active=false)
    @MessageMapping("/toll/{roomId}")
    public void handleToll(@DestinationVariable Long roomId,
                           @Payload Map<String, Integer> payload,
                           SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int landId = payload.get("landId");
        boardGameService.toll(roomId, userId, landId);

        webSocketService.sendGame(roomId, "toll", gameType, boardPlayerService.getPlayers(roomId));
    }

    // 퀴즈 요청 -> 퀴즈 테이블 중 하나 랜덤 전송

    // 퀴즈 풀기 -> 클라이언트가 정답 처리 후 보낸 결과값만 처리

    // 저금/세금+파산처리(active=false)
    @MessageMapping("/tax/{roomId}")
    public void handleBank(@DestinationVariable Long roomId,
                           @Payload Map<String, Integer> payload,
                           SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("roomState", roomStateService.getState(roomId));

        webSocketService.sendGame(roomId, "bank", gameType, data);
    }

    // 금고/월급
    @MessageMapping("/salary/{roomId}")
        public void handleSalary(@DestinationVariable Long roomId,
                               @Payload Map<String, Integer> payload,
                               SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int landId = payload.get("landId");
        boardGameService.salary(roomId, userId, landId);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("roomState", roomStateService.getState(roomId));

        webSocketService.sendGame(roomId, "salary", gameType, data);
    }

    // 무인도 -> skipTurn=ture

    // 턴 종료 시 다음 턴 사람이 skipTurn이라면 같이 종료?

    // 라운드 종료, 마지막 라운드라면 게임 결과 반환 후 페이지 이동
}
