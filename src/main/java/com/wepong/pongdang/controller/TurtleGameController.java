package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.entity.enums.GameRoomStatus;
import com.wepong.pongdang.model.multi.turtle.TurtleGameService;
import com.wepong.pongdang.model.multi.turtle.TurtlePlayerService;
import com.wepong.pongdang.service.GameRoomService;
import com.wepong.pongdang.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TurtleGameController {

    private final GameRoomService gameRoomService;
    private final TurtleGameService turtleGameService;
    private final WebSocketService webSocketService;
    private final TurtlePlayerService turtlePlayerService;

    // 게임 진행
    @MessageMapping("/turtle/start/{roomId}")
    public void startGame(@DestinationVariable Long roomId, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        int turtleCount = switch (gameroom.getLevel()) {
            case EASY -> 4;
            case NORMAL -> 6;
            case HARD -> 8;
        };

        // 랜덤 거북이 세팅
        turtlePlayerService.setRandomTurtle(userId, roomId, turtleCount);

        turtleGameService.startGame(roomId, turtleCount, new TurtleGameService.RaceUpdateCallback() {
            @Override
            public void onRaceUpdate(Long roomId, double[] positions) {
                turtleGameService.broadcastRaceUpdate(roomId, positions, gameType);
            }

            @Override
            public void onRaceFinish(Long roomId, int winner, List<Map<String, Object>> results) {
                turtleGameService.broadcastRaceFinish(roomId, winner, results, gameType);
            }
        });

        Long hostId = gameroom.getHostId();
        if (userId.equals(hostId)) {
            turtleGameService.onGameStart(roomId, gameType); // 참가자 freeze
        }
    }

    @MessageMapping("/game/end/{roomId}")
    public void endGame(@DestinationVariable Long roomId, SimpMessageHeaderAccessor accessor) {
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        GameRoomResponseDTO.GameRoomDetailDTO room = gameRoomService.selectById(roomId);
        if(!room.getStatus().equals(GameRoomStatus.WAITING)) {
            gameRoomService.updateStatus(roomId, GameRoomStatus.WAITING);
            webSocketService.sendList(gameRoomService.selectAll());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("gameType", gameType);
        data.put("roomId", roomId);

        webSocketService.sendGame(roomId,"end", gameType, data);
    }
}
