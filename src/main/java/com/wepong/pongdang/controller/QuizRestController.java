package com.wepong.pongdang.controller;


import com.wepong.pongdang.dto.request.QuizRequestDTO;
import com.wepong.pongdang.dto.response.QuizResponseDTO;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.QuizEntity;
import com.wepong.pongdang.entity.UserEntity;
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

    // (관리) 오늘 세트 생성/갱신
    @PostMapping("/generate")
    public ResponseEntity<List<QuizResponseDTO.QuizView>> generateToday() {
        // 1) 오늘 문제 생성 & 저장
        quizService.generateTodayAndSave();

        // 2) 중복된 문제 있으면 재생성 → DB update
        quizService.regenerateDuplicates();

        // 3) 최종 확정된 오늘 문제 다시 읽어오기
        List<QuizEntity> saved = quizService.getToday();

        // 4) DTO 변환해서 반환
        return ResponseEntity.ok(
                saved.stream()
                        .map(QuizEntity::toDto)
                        .toList()
        );
    }

    // 오늘 세트 조회(사용자용)
    @GetMapping("")
    public ResponseEntity<List<QuizResponseDTO.QuizView>> today(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // 로그인 안 했거나 토큰 비어있으면 예외 처리
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
        }

        List<QuizEntity> list = quizService.getToday();

        // ✅ 오늘 퀴즈가 없으면 자동 생성 후 다시 조회
        if (list.isEmpty()) {
            quizService.generateTodayAndSave();
            quizService.regenerateDuplicates();
            list = quizService.getToday();
        }

        return ResponseEntity.ok(
                list.stream()
                        .map(QuizEntity::toDto)
                        .toList()
        );
    }

    // 오늘 퀴즈 풀이 체크
    @PostMapping("/check")
    public ResponseEntity<?> markTodayQuizTaken(
            @RequestHeader("Authorization") String authHeader
    ) {
        // 🔑 토큰에서 userId 추출 (서비스나 util에서 처리)
        Long userId = authService.validateAndGetUserId(authHeader);

        quizCheckService.markTodayQuizTaken(userId);

        return ResponseEntity.ok(Map.of(
                "message", "오늘 퀴즈 참여가 기록되었습니다."
        ));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody QuizRequestDTO request
    ) {
        Long id = authService.validateAndGetUserId(authHeader);

        int reward = Math.min(
                request.getCorrectCount() != null ? request.getCorrectCount() : 0,
                3
        ); // 최대 3 포인트 제한

        if (reward > 0) {
            // 포인트 히스토리 기록
            PongHistoryEntity history = PongHistoryEntity.builder()
                    .type(PongHistoryType.ADD)
                    .amount(reward)
                    .build();

            historyService.insertPointHistory(history, id);

            // 실제 포인트 지급
            walletService.add(reward, id, WalletType.PONG);
        }

        return ResponseEntity.ok(Map.of(
                "message", "퀴즈 종료",
                "reward", reward
        ));
    }


}

