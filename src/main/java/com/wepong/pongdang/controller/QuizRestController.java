package com.wepong.pongdang.controller;


import com.wepong.pongdang.dto.request.QuizRequestDTO;
import com.wepong.pongdang.dto.response.QuizResponseDTO;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.QuizEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.UnauthorizedAccessException;
import com.wepong.pongdang.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class QuizRestController {

    private final AuthService authService;
    private final QuizService quizService;
    private final QuizCheckService quizCheckService;
    private final HistoryService historyService;
    private final WalletService walletService;

    // 오늘 세트 조회(사용자용)
    @GetMapping("")
    public List<QuizResponseDTO.QuizView> today(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
        }

        return quizService.getTodayWithAutoGenerate();
    }

    // 오늘 퀴즈 풀이 체크
    @PostMapping("/check")
    public QuizResponseDTO.QuizCheckResponse markTodayQuizTaken(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return quizCheckService.markTodayQuizTaken(userId);
    }

    // 퀴즈 제출
    @PostMapping("/submit")
    public QuizResponseDTO.QuizSubmitResponse submitQuiz(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody QuizRequestDTO request
    ) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return quizCheckService.submitQuiz(userId, request);
    }

}

