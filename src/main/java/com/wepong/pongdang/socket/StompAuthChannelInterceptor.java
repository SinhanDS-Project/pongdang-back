package com.wepong.pongdang.socket;

import com.wepong.pongdang.exception.InvalidTokenException;
import com.wepong.pongdang.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final AuthService authService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == null) {
            return message;
        }

        // STOMP 연결 요청 시
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 헤더에서 토큰 추출 후 검증
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            // 토큰이 있을 때만 검증
            if (authHeader != null && !authHeader.isEmpty()) {
                Long userId = authService.validateAndGetUserId(authHeader);
                accessor.getSessionAttributes().put("userId", userId);
            }

        } else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            // 구독 경로 추출
            if(destination != null) {
                if(destination.startsWith("/topic/gameroom/")) {
                    Long userId = (Long) accessor.getSessionAttributes().get("userId");
                    if (userId == null) {
                        throw new InvalidTokenException();
                    }

                    String nickname = authService.findById(userId).getNickname();
                    accessor.getSessionAttributes().put("nickname", nickname);

                    parseAndStoreGameInfo(destination, "/topic/gameroom/", accessor, "room");
                }
                else if(destination.startsWith("/topic/game/")) {
                    parseAndStoreGameInfo(destination, "/topic/game/", accessor, "game");
                }
                else if(destination.equals("/topic/gameroom")) {
                    accessor.getSessionAttributes().put("type", "list");
                }
            }
        }

        // 메시지 그대로 컨트롤러에 반환
        return message;
    }

    // gameType, roomId 추출 후 세션에 저장
    private void parseAndStoreGameInfo(String destination, String prefix,
                                       StompHeaderAccessor accessor, String suffix) {
        // ✅ gameType, roomId 분리
        String path = destination.substring(prefix.length());
        String[] parts = path.split("/");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid destination path: " + destination);
        }

        String gameType = parts[0];
        Long roomId = Long.parseLong(parts[1]);

        accessor.getSessionAttributes().put("roomId", roomId);
        accessor.getSessionAttributes().put("type", gameType+"room");
        accessor.getSessionAttributes().put("gameType", gameType);
    }
}
