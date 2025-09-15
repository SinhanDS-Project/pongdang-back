package com.wepong.pongdang.socket;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.model.multi.board.BoardGameService;
import com.wepong.pongdang.model.multi.board.BoardPlayerDTO;
import com.wepong.pongdang.model.multi.board.BoardPlayerService;
import com.wepong.pongdang.model.multi.turtle.TurtlePlayerDTO;
import com.wepong.pongdang.entity.enums.GameRoomStatus;
import com.wepong.pongdang.model.multi.turtle.TurtlePlayerService;
import com.wepong.pongdang.model.multi.turtle.TurtleGameService;
import com.wepong.pongdang.service.GameRoomService;
import com.wepong.pongdang.service.WebSocketService;
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
    private final TurtlePlayerService turtlePlayerService;
    private final TurtleGameService turtleGameService;
    private final BoardPlayerService boardPlayerService;
    private final BoardGameService boardGameService;
    private final WebSocketService webSocketService;

    private final Map<Long, List<TurtlePlayerDTO>> startTurtlePlayersMap = new ConcurrentHashMap<>();
    private final Map<Long, List<BoardPlayerDTO>> startBoardPlayersMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleConnect(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String type = (String) accessor.getSessionAttributes().get("type");

        // 게임 대기방 리스트 연결 시
        if(type.equals("list")) {
            return;
        }

        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        Long roomId = (Long) accessor.getSessionAttributes().get("roomId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        if(type.equals("turtlegame")) {
            // (1) 게임 시작 전이면 검증 건너뛰고, 그냥 세션 등록
            List<TurtlePlayerDTO> startPlayers = startTurtlePlayersMap.get(roomId);
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
                msg.put("targetUrl", "/game/rooms");
                webSocketService.sendGame(roomId, "force_exit", gameType, msg);
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        } else if(type.equals("boardgame")) {
            List<BoardPlayerDTO> startPlayers = startBoardPlayersMap.get(roomId);
            if (startPlayers == null) {
                return;
            }

            boolean inGame = false;
            for (BoardPlayerDTO player : startPlayers) {
                if (player.getUserId().equals(userId)) {
                    inGame = true;
                    break;
                }
            }

            if (!inGame) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("reason", "no_player_info");
                msg.put("targetUrl", "/game/rooms");
                webSocketService.sendGame(roomId, "force_exit", gameType, msg);
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String type = (String) accessor.getSessionAttributes().get("type");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        // 게임 대기방 리스트 연결 종료 시
        if(type.equals("list")) {
            return;
        }

        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        Long roomId = (Long) accessor.getSessionAttributes().get("roomId");

        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        GameRoomStatus status = gameroom.getStatus();

        if("turtleroom".equals(type)) {
            List<TurtlePlayerDTO> players;
            if(!status.equals(GameRoomStatus.PLAYING)) {
                turtlePlayerService.exitPlayer(roomId, userId);
                players = turtlePlayerService.getPlayers(roomId);

                if(userId.equals(gameroom.getHostId())) {
                    if(players != null && !players.isEmpty()) {
                        Long hostId = players.get(0).getUserId();
                        gameRoomService.updateHost(roomId, hostId);

                    } else {
                        gameRoomService.deleteRoom(roomId);
                        webSocketService.sendList(gameRoomService.selectAll());
                    }
                }

                webSocketService.sendRoom(roomId, "exit", gameType, players);
            }
        } else if("turtlegame".equals(type)) {
            List<TurtlePlayerDTO> players;
            if(!status.equals(GameRoomStatus.WAITING)) {
                turtleGameService.processUserLose(roomId, userId);

                turtlePlayerService.exitPlayer(roomId, userId);
                players = turtlePlayerService.getPlayers(roomId);

                if (userId.equals(gameroom.getHostId())) {
                    if (players != null && !players.isEmpty()) {
                        Long hostId = players.get(0).getUserId();
                        gameRoomService.updateHost(roomId, hostId);

                    } else {
                        turtleGameService.removeGame(roomId);
                        webSocketService.sendList(gameRoomService.selectAll());
                    }
                }

                if(!event.getCloseStatus().equals(CloseStatus.NORMAL)) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("reason", "connection_error");
                    msg.put("targetUrl", "/game/rooms");
                    webSocketService.sendGame(roomId, "force_exit", gameType, msg);
                }
            }
        } else if("boardroom".equals(type)) {
            List<BoardPlayerDTO> players;
            if(!status.equals(GameRoomStatus.PLAYING)) {
                boardPlayerService.exitPlayer(roomId, userId);
                players = boardPlayerService.getPlayers(roomId);

                if(userId.equals(gameroom.getHostId())) {
                    if(players != null && !players.isEmpty()) {
                        Long hostId = players.get(0).getUserId();
                        gameRoomService.updateHost(roomId, hostId);

                    } else {
                        gameRoomService.deleteRoom(roomId);
                        webSocketService.sendList(gameRoomService.selectAll());
                    }
                }

                webSocketService.sendRoom(roomId, "exit", gameType, players);
            }
        } else if("boardgame".equals(type)) {
            List<BoardPlayerDTO> players;
            if(!status.equals(GameRoomStatus.WAITING)) {
                boardGameService.processUserLose(roomId, userId);

                boardPlayerService.exitPlayer(roomId, userId);
                players = boardPlayerService.getPlayers(roomId);

                if (userId.equals(gameroom.getHostId())) {
                    if (players != null && !players.isEmpty()) {
                        Long hostId = players.get(0).getUserId();
                        gameRoomService.updateHost(roomId, hostId);

                    } else {
                        turtleGameService.removeGame(roomId);
                        webSocketService.sendList(gameRoomService.selectAll());
                    }
                }

                if(!event.getCloseStatus().equals(CloseStatus.NORMAL)) {
                    Map<String, Object> msg = new HashMap<>();
                    msg.put("reason", "connection_error");
                    msg.put("targetUrl", "/game/rooms");
                    webSocketService.sendGame(roomId, "force_exit", gameType, msg);
                }
            }
        }
        webSocketService.sendList(gameRoomService.selectAll());
    }
}