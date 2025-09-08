package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.ChatResponseDTO;
import com.wepong.pongdang.dto.response.TurtlePlayerDTO;
import com.wepong.pongdang.model.multi.turtle.PlayerService;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class GameRoomController {

    private final AuthService authService;
    private final PlayerService playerService;
    private final GameRoomService gameRoomService;

    // 채팅
    @MessageMapping("/gameroom/chat/{roomId}")
    public void handleGameAction(@DestinationVariable Long roomId, String msg, StompHeaderAccessor accessor) {
        String nickname = (String) accessor.getSessionAttributes().get("nickname");

        ChatResponseDTO chat = ChatResponseDTO.builder()
                .message(msg)
                .sender(nickname)
                .build();

        gameRoomService.sendRoom(roomId, "chat", chat);
    }

    // 거북이 선택
    @MessageMapping("/gameroom/choice/{roomId}")
    public void handleChoice(@DestinationVariable Long roomId, Map<String, String> payload, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        TurtlePlayerDTO player = playerService.getPlayer(roomId, userId);
        player.setTurtleId(payload.get("turtle_id"));

        List<TurtlePlayerDTO> players = playerService.getPlayers(roomId);
        gameRoomService.sendRoom(roomId, "choice", players);
    }

    // 준비 완료
    @MessageMapping("/gameroom/ready/{roomId}")
    public void handleReady(@DestinationVariable Long roomId, Map<String, Boolean> payload, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        TurtlePlayerDTO player = playerService.getPlayer(roomId, userId);
        player.setReady(payload.get("isReady"));

        List<TurtlePlayerDTO> players = playerService.getPlayers(roomId);
        gameRoomService.sendRoom(roomId, "ready", players);
    }

    // 게임 시작
    @MessageMapping("/gameroom/start/{roomId}")
    public void handleStart(@DestinationVariable Long roomId) {
        gameRoomService.sendRoom(roomId, "start", "/multi/" + roomId + "/turtlerun");
        gameRoomService.sendList("list", gameRoomService.selectAll());
    }
}
