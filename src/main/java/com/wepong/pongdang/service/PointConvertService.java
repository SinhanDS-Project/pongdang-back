package com.wepong.pongdang.service;

import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.WalletType;
import lombok.RequiredArgsConstructor;
import net.wepong.mysql.service.BettingUserPointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointConvertService {

    private final BettingUserPointService bettingUserPointService;
    private final WalletService walletService;                     // pongdang DB
    private final AuthService authService;

    @Transactional
    public int convert(String bettingUid, int amount, Long pongUserId) {
        // 1) betting 차감
        bettingUserPointService.deductPoint(bettingUid, amount);
        UserEntity user = authService.findById(pongUserId);

        // 2) 환산 로직 (100원 = 1퐁)
        int pongAmount = amount / 100;

        // 3) pongdang 적립
        try {
            walletService.convert(pongAmount, user, WalletType.PONG);
        } catch (RuntimeException e) {
            // 보상 트랜잭션: betting 되돌리기
            bettingUserPointService.addPoint(bettingUid, amount);
            throw e;
        }

        return pongAmount; // 전환된 퐁 단위 반환
    }
}
