package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.WalletRequestDTO;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final AuthService authService;

    @PutMapping("/add")
    public void addWallet(@RequestBody WalletRequestDTO request,
                             @RequestHeader(value = "Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        walletService.add(request.getAmount(), userId, request.getWalletType());
    }

    @PutMapping("/lose")
    public void loseWallet(@RequestBody WalletRequestDTO request,
                             @RequestHeader(value = "Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        walletService.lose(request.getAmount(), userId, request.getWalletType());
    }
}
