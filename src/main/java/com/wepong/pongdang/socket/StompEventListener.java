package com.wepong.pongdang.socket;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.dto.response.TurtlePlayerDTO;
import com.wepong.pongdang.entity.enums.GameRoomStatus;
import com.wepong.pongdang.model.multi.turtle.PlayerService;
import com.wepong.pongdang.model.multi.turtle.TurtleGameService;
import com.wepong.pongdang.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class StompEventListener {

    private final GameRoomService gameRoomService;
    private final PlayerService playerService;
    private final TurtleGameService turtleGameService;

    private final Map<Long, List<TurtlePlayerDTO>> gameStartPlayersMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleConnect(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        Long roomId = (Long) accessor.getSessionAttributes().get("roomId");

        if(accessor.getSessionAttributes().get("type").equals("room")) {
            // 중복 입장 처리
            if (playerService.exists(roomId, userId)) {
                playerService.exitPlayer(roomId, userId);
            }

            playerService.enterPlayer(userId, roomId);

            TurtlePlayerDTO player = playerService.getPlayer(roomId, userId);
            accessor.getSessionAttributes().put("nickname", player.getNickname());

            List<TurtlePlayerDTO> players = playerService.getPlayers(roomId);
            gameRoomService.sendRoom(roomId, "enter", players);
            gameRoomService.sendList("list", gameRoomService.selectAll());

        } else if(accessor.getSessionAttributes().get("type").equals("game")) {
            // (1) 게임 시작 전이면 검증 건너뛰고, 그냥 세션 등록
            List<TurtlePlayerDTO> startPlayers = gameStartPlayersMap.get(roomId);
            if (startPlayers == null) {
                return;
            }

            // (2) 게임 시작 후에는 userId가 freeze 목록에 없으면 강제퇴장
            boolean inGame = false;
            for (TurtlePlayerDTO player : startPlayers) {
                if (player.getUserId().equals(userId)) {
                    inGame = true;
                    break;
                }
            }

            if (!inGame) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("reason", "no_player_info");
                msg.put("targetUrl", "/gameroom");
                gameRoomService.sendGame(roomId, "force_exit", msg);
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        } else if(accessor.getSessionAttributes().get("type").equals("list")) {
            gameRoomService.sendList("list", gameRoomService.selectAll());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String type = (String) accessor.getSessionAttributes().get("type");
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        Long roomId = (Long) accessor.getSessionAttributes().get("roomId");

        if(roomId == null || userId == null) return;

        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        GameRoomStatus status = gameroom.getStatus();

        List<TurtlePlayerDTO> players;

        if("room".equals(type)) {
            if(!status.equals(GameRoomStatus.PLAYING)) {
                playerService.exitPlayer(roomId, userId);
                players = playerService.getPlayers(roomId);

                if(userId.equals(gameroom.getHostId())) {
                    if(players != null && !players.isEmpty()) {
                        Long hostId = players.get(0).getUserId();
                        gameRoomService.updateHost(roomId, hostId);

                    } else {
                        gameRoomService.deleteRoom(roomId);
                        gameRoomService.sendList("delete", gameRoomService.selectAll());
                    }
                }

                gameRoomService.sendRoom(roomId, "exit", players);
            }
        } else if("game".equals(type)) {
            if(!status.equals(GameRoomStatus.WAITING)) {
                turtleGameService.processUserLose(roomId, userId);

                playerService.exitPlayer(roomId, userId);
                players = playerService.getPlayers(roomId);

                if (userId.equals(gameroom.getHostId())) {
                    if (players != null && !players.isEmpty()) {
                        Long hostId = players.get(0).getUserId();
                        gameRoomService.updateHost(roomId, hostId);

                    } else {
                        turtleGameService.removeGame(roomId);
                        gameRoomService.sendList("delete", gameRoomService.selectAll());
                    }
                }

                if(!event.getCloseStatus().equals(CloseStatus.NORMAL)) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("reason", "connection_error");
                    msg.put("targetUrl", "/gameroom");
                    gameRoomService.sendGame(roomId, "force_exit", msg);
                }
            }
        }
        gameRoomService.sendList("list", gameRoomService.selectAll());
    }
}