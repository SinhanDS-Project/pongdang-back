package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.model.multi.turtle.TurtleGameService;
import com.wepong.pongdang.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TurtleGameController {

    private final GameRoomService gameRoomService;
    private final TurtleGameService turtleGameService;

    // 게임 진행
    @MessageMapping("/game/start/{roomId}")
    public void startGame(@DestinationVariable Long roomId, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        int turtleCount = switch (gameroom.getLevel()) {
            case EASY -> 4;
            case NORMAL -> 6;
            case HARD -> 8;
        };

        turtleGameService.startGame(roomId, turtleCount, new TurtleGameService.RaceUpdateCallback() {
            @Override
            public void onRaceUpdate(Long roomId, double[] positions) {
                turtleGameService.broadcastRaceUpdate(roomId, positions);
            }

            @Override
            public void onRaceFinish(Long roomId, int winner, List<Map<String, Object>> results) {
                turtleGameService.broadcastRaceFinish(roomId, winner, results);
            }
        });

        Long hostId = gameroom.getHostId();
        if (userId.equals(hostId)) {
            turtleGameService.onGameStart(roomId); // 참가자 freeze
        }
    }

    @MessageMapping("/game/end/{roomId}")
    public void endGame(@DestinationVariable Long roomId) {
        gameRoomService.sendGame(roomId, "end", "/gameroom/detail/" + roomId);
    }
}
