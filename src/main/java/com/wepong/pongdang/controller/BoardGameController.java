package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.QuizResponseDTO;
import com.wepong.pongdang.model.multi.board.*;
import com.wepong.pongdang.service.QuizService;
import com.wepong.pongdang.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BoardGameController {

    private final BoardPlayerService boardPlayerService;
    private final BoardGameService boardGameService;
    private final QuizService quizService;
    private final WebSocketService webSocketService;

    // 게임 시작
    @MessageMapping("/board/start/{roomId}")
    public void startGame(@DestinationVariable Long roomId, SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        // 랜덤 색상 세팅
        boardPlayerService.setRandomTurtle(userId, roomId);

        // RoomState, Land 메모리 생성 후 전송
        boardGameService.startGame(roomId, gameType);
    }

    // 주사위 굴리기 -> dice 값만큼 포지션 +
    @MessageMapping("/roll/{roomId}")
    public void handleRoll(@DestinationVariable Long roomId,
                             @Payload Map<String, Object> payload,
                             SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int dice = (int) payload.get("dice");
        boolean isDouble = (boolean) payload.get("isDouble");
        boardGameService.roll(roomId, userId, dice, isDouble, gameType);
    }

    // 땅 구매
    @MessageMapping("/purchase/{roomId}")
    public void handlePurchase(@DestinationVariable Long roomId,
                          @Payload Map<String, Object> payload,
                          SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int landId = (int) payload.get("landId");
        boardGameService.purchase(roomId, userId, landId, gameType);
    }

    // 통행료 지불+파산처리(active=false)
    @MessageMapping("/toll/{roomId}")
    public void handleToll(@DestinationVariable Long roomId,
                           @Payload Map<String, Object> payload,
                           SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int landId = (int) payload.get("landId");
        boardGameService.toll(roomId, userId, landId, gameType);
    }

    // 퀴즈 요청 -> 퀴즈 테이블 중 하나 랜덤 전송
    @MessageMapping("/quiz/{roomId}")
    public void handleQuiz(@DestinationVariable Long roomId,
                           SimpMessageHeaderAccessor accessor) {
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        QuizResponseDTO.QuizView quiz = quizService.getRandomQuiz();
        webSocketService.sendGame(roomId, "quiz", gameType, quiz);
    }

    // 퀴즈 풀기 -> 클라이언트가 정답 처리 후 보낸 결과값만 처리
    @MessageMapping("/quiz/check/{roomId}")
    public void handleCheck(@DestinationVariable Long roomId,
                            @Payload Map<String, Object> payload,
                            SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int selectIdx = (int) payload.get("selectIdx");
        boolean isCorrect = (boolean) payload.get("isCorrect");
        boardGameService.quiz(roomId, userId, selectIdx, isCorrect, gameType);
    }

    // 저금/세금+파산처리(active=false)
    @MessageMapping("/tax/{roomId}")
    public void handleBank(@DestinationVariable Long roomId,
                           @Payload Map<String, Object> payload,
                           SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int landId = (int) payload.get("landId");
        boardGameService.bank(roomId, userId, landId, gameType);
    }

    // 금고/월급
    @MessageMapping("/salary/{roomId}")
        public void handleSalary(@DestinationVariable Long roomId,
                               @Payload Map<String, Object> payload,
                               SimpMessageHeaderAccessor accessor) {
        Long userId = (Long) accessor.getSessionAttributes().get("userId");
        String gameType = (String) accessor.getSessionAttributes().get("gameType");

        int landId = (int) payload.get("landId");
        boardGameService.salary(roomId, userId, landId, gameType);
    }
}
