package com.wepong.pongdang.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

// 웹소켓 설정
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompAuthChannelInterceptor channelInterceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// STOMP 연결 엔드포인트는 하나로 관리
		// 메시지 처리는 각 구독/발행 경로로 관리
		registry.addEndpoint("/ws")
				.setAllowedOriginPatterns("http://localhost:3000")
				.withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(channelInterceptor);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic", "/queue");
		registry.setApplicationDestinationPrefixes("/app");
	}
}
