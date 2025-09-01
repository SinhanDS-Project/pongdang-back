package com.wepong.pongdang.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class RawWebSocketConfig implements WebSocketConfigurer {

    private final GameRoomWebSocketHandler gameRoomWebSocketHandler;
    private final TurtleHandshakeInterceptor turtleHandshakeInterceptor;
    private final TurtleRunWebsocketHandler turtleRunWebsocketHandler;
    private final GameRoomListWebSocket gameRoomListWebSocket;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameRoomWebSocketHandler, "/ws/game/turtleroom/**")
                .setAllowedOrigins("*")
                .addInterceptors(turtleHandshakeInterceptor);

        registry.addHandler(turtleRunWebsocketHandler, "/ws/game/turtle/**")
                // 다른 게임에서 사용 시 엔드포인트 추가
                .setAllowedOrigins("*")
                .addInterceptors(turtleHandshakeInterceptor);

        registry.addHandler(gameRoomListWebSocket, "/ws/gameroom")
                .setAllowedOrigins("*");
    }
}