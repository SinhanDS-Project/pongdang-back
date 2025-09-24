package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.WalletRequestDTO;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.exception.UnauthorizedAccessException;
import com.wepong.pongdang.service.AttendanceService;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final AuthService authService;
    private final AttendanceService attendanceService;

    @PutMapping("/add")
    public ResponseEntity<?> addWallet(@RequestBody WalletRequestDTO request,
                                       @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
        }
        Long userId = authService.validateAndGetUserId(authHeader);
        UserEntity user = authService.findById(userId);

        walletService.add(request.getAmount(), user, request.getWalletType());
        attendanceService.eventCheck(request.getEventType(), userId);
        return ResponseEntity.ok(Map.of("message", "퐁이 적립되었습니다."));
    }
}
