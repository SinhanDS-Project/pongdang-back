package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.WalletRequestDTO;
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
                                    @RequestHeader(value = "Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        walletService.add(request.getAmount(), userId, request.getWalletType());
        attendanceService.eventCheck(request.getEventType(), userId);
        return ResponseEntity.ok(Map.of("message", "퐁이 적립되었습니다."));
    }

    @PutMapping("/lose")
    public ResponseEntity<?> loseWallet(@RequestBody WalletRequestDTO request,
                             @RequestHeader(value = "Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        walletService.lose(request.getAmount(), userId, request.getWalletType());
        return ResponseEntity.ok(Map.of("message", "퐁이 차감되었습니다."));
    }
}
