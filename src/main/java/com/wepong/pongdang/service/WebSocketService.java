package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.response.WebSocketResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendRoom(Long roomId, String type, String gameType, Object data) {
        WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
                .type(type)
                .data(data)
                .build();

        messagingTemplate.convertAndSend("/topic/gameroom/" + gameType + "/" + roomId, payload);
    }

    public void sendList(Object data) {
        WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
                .type("list")
                .data(data)
                .build();

        messagingTemplate.convertAndSend("/topic/gameroom", payload);
    }

    public void sendGame(Long roomId, String type, String gameType, Object data) {
        WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
                .type(type)
                .data(data)
                .build();

        messagingTemplate.convertAndSend("/topic/game/" + gameType + "/" + roomId, payload);
    }

    public void sendGame(Long roomId, String gameType, String type) {
        WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
                .type(type)
                .build();

        messagingTemplate.convertAndSend("/topic/game/" + gameType + "/" + roomId, payload);
    }

    public void sendMain(String type, Object data) {
        WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
                .type(type)
                .data(data)
                .build();
        messagingTemplate.convertAndSend("/topic/notice", payload);
    }
}
