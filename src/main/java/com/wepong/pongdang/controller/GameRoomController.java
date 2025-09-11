package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.ChatResponseDTO;
import com.wepong.pongdang.dto.response.TurtlePlayerDTO;
import com.wepong.pongdang.model.multi.turtle.PlayerService;
import com.wepong.pongdang.service.GameRoomService;
import com.wepong.pongdang.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class GameRoomController {

    private final PlayerService playerService;
    private final WebSocketService webSocketService;
    private final GameRoomService gameRoomService;

    // 입장
    @MessageMapping("/gameroom/enter/{roomId}")
        public void handleEnter(@DestinationVariable Long roomId, StompHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");

        // 중복 입장 처리
        if (playerService.exists(roomId, userId)) {
            playerService.exitPlayer(roomId, userId);
        }

        playerService.enterPlayer(roomId, userId);

        List<TurtlePlayerDTO> players = playerService.getPlayers(roomId);
        webSocketService.sendRoom(roomId, "enter", players);
        webSocketService.sendList(gameRoomService.selectAll());
    }

    // 채팅
    @MessageMapping("/gameroom/chat/{roomId}")
    public void handleChat(@DestinationVariable Long roomId, @Payload Map<String, String> payload, StompHeaderAccessor accessor) {
        String nickname = (String) accessor.getSessionAttributes().get("nickname");
        String msg = payload.get("msg");

        ChatResponseDTO chat = ChatResponseDTO.builder()
                .message(msg)
                .sender(nickname)
                .build();

        webSocketService.sendRoom(roomId, "chat", chat);
    }

    // 거북이 선택
    @MessageMapping("/gameroom/choice/{roomId}")
    public void handleChoice(@DestinationVariable Long roomId, @Payload Map<String, String> payload, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        TurtlePlayerDTO player = playerService.getPlayer(roomId, userId);
        player.setTurtleId(payload.get("turtle_id"));

        List<TurtlePlayerDTO> players = playerService.getPlayers(roomId);
        webSocketService.sendRoom(roomId, "choice", players);
    }

    // 준비 완료/취소
    @MessageMapping("/gameroom/ready/{roomId}")
    public void handleReady(@DestinationVariable Long roomId, @Payload Map<String, Boolean> payload, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        TurtlePlayerDTO player = playerService.getPlayer(roomId, userId);
        player.setReady(payload.get("isReady"));

        List<TurtlePlayerDTO> players = playerService.getPlayers(roomId);
        webSocketService.sendRoom(roomId, "ready", players);
    }

    // 게임 시작
    @MessageMapping("/gameroom/start/{roomId}")
    public void handleStart(@DestinationVariable Long roomId) {
        webSocketService.sendRoom(roomId, "start", "/play/" + roomId);
    }
}
