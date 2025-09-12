package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.ChatResponseDTO;
import com.wepong.pongdang.model.multi.board.BoardPlayerDTO;
import com.wepong.pongdang.model.multi.board.BoardPlayerService;
import com.wepong.pongdang.model.multi.turtle.TurtlePlayerDTO;
import com.wepong.pongdang.model.multi.turtle.TurtlePlayerService;
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

    private final TurtlePlayerService turtlePlayerService;
    private final WebSocketService webSocketService;
    private final GameRoomService gameRoomService;
    private final BoardPlayerService boardPlayerService;

    // 게임 입장
    @MessageMapping("/gameroom/enter/{roomId}")
        public void handleEnter(@DestinationVariable Long roomId, StompHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String type = (String) accessor.getSessionAttributes().get("type");

        List<?> players = null;

        // 거북이
        if(type.equals("turtleroom")) {
            // 중복 입장 처리
            if (turtlePlayerService.exists(roomId, userId)) {
                turtlePlayerService.exitPlayer(roomId, userId);
            }

            turtlePlayerService.enterPlayer(roomId, userId);

            players = turtlePlayerService.getPlayers(roomId);
            webSocketService.sendRoom(roomId, "enter", players);
        }
        // 보드
        else if(type.equals("boardroom")) {
            if (boardPlayerService.exists(roomId, userId)) {
                boardPlayerService.exitPlayer(roomId, userId);
            }

            boardPlayerService.enterPlayer(roomId, userId);

            players = boardPlayerService.getPlayers(roomId);
            webSocketService.sendRoom(roomId, "enter", players);
        }

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
        String type = (String) accessor.getSessionAttributes().get("type");

        List<?> players = null;

        // 거북이
        if(type.equals("turtleroom")) {
            TurtlePlayerDTO player = turtlePlayerService.getPlayer(roomId, userId);
            player.setTurtleId(payload.get("turtle_id"));

            players = turtlePlayerService.getPlayers(roomId);
        } else if(type.equals("boardroom")) {
            BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
            player.setTurtleId(payload.get("turtle_id"));

            players = boardPlayerService.getPlayers(roomId);
        }

        webSocketService.sendRoom(roomId, "choice", players);
    }

    // 준비 완료/취소
    @MessageMapping("/gameroom/ready/{roomId}")
    public void handleReady(@DestinationVariable Long roomId, @Payload Map<String, Boolean> payload, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String type = (String) accessor.getSessionAttributes().get("type");

        List<?> players = null;

        if(type.equals("turtleroom")) {
            TurtlePlayerDTO player = turtlePlayerService.getPlayer(roomId, userId);
            player.setReady(payload.get("isReady"));

            players = turtlePlayerService.getPlayers(roomId);
        } else if(type.equals("boardroom")) {
            BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
            player.setReady(payload.get("isReady"));

            players = boardPlayerService.getPlayers(roomId);
        }

        webSocketService.sendRoom(roomId, "ready", players);
    }

    // 게임 시작
    @MessageMapping("/gameroom/start/{roomId}")
    public void handleStart(@DestinationVariable Long roomId) {
        webSocketService.sendRoom(roomId, "start", "/play/" + roomId);
    }
}
