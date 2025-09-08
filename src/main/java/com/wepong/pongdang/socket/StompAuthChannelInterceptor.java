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

import java.util.List;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final AuthService authService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // STOMP 연결 요청 시
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 헤더에서 토큰 추출 후 검증
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || authHeader.isEmpty()) {
                throw new InvalidTokenException();
            }

            Long userId = authService.validateAndGetUserId(authHeader);
            accessor.getSessionAttributes().put("userId", userId);
        } else if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            // 구독 경로 추출
            if(destination != null) {
                if(destination.startsWith("/topic/gameroom")) {
                    Long roomId = Long.parseLong(destination.substring("/topic/gameroom".length()));
                    accessor.getSessionAttributes().put("roomId", roomId);
                    accessor.getSessionAttributes().put("type", "room");
                }
                else if(destination.startsWith("/topic/game")) {
                    accessor.getSessionAttributes().put("type", "game");
                }
                else if(destination.equals("/topic/gameroom")) {
                    accessor.getSessionAttributes().put("type", "list");
                }
            }
        }

        // 메시지 그대로 컨트롤러에 반환
        return message;
    }
}
