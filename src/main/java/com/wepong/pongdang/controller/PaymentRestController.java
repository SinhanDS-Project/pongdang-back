package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.PaymentConfirmDTO;
import com.wepong.pongdang.dto.response.PaymentResponseDTO;
import com.wepong.pongdang.entity.PaymentEntity;
import com.wepong.pongdang.entity.enums.PaymentStatus;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.HistoryService;
import com.wepong.pongdang.service.PaymentService;
import com.wepong.pongdang.service.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuthService authService;

    @Autowired
    private HistoryService historyService;
	@Autowired
	private WalletService walletService;

    @PostMapping("/confirm")
    public PaymentResponseDTO confirmPayment(@RequestBody PaymentConfirmDTO response,
                                             @RequestHeader("Authorization") String authHeader) throws Exception {
        Long userId = authService.validateAndGetUserId(authHeader);
        PaymentEntity paymentEntity = paymentService.confirmPayment(response, userId);

        if(paymentEntity.getStatus().equals(PaymentStatus.PAID)) {
            int netPoint = (int) Math.round(paymentEntity.getAmount() / 1.1);
            walletService.add(netPoint, userId, WalletType.PONG);
            historyService.insertPointHistory(userId, netPoint);
        }

        return PaymentResponseDTO.from(paymentEntity);
    }
}
