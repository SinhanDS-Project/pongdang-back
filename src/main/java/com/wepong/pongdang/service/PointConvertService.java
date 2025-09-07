package com.wepong.pongdang.service;

import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import net.wepong.mysql.repository.SUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointConvertService {

    private final SUserRepository sUserRepository;   // betting DB
    private final WalletService walletService;       // pongdang DB

    @Transactional
    public int convert(String bettingUid, int amount, Long pongUserId) {
        // 1) betting 차감
        int updated = sUserRepository.tryDeductPoint(bettingUid, amount);
        if (updated == 0) {
            throw new IllegalArgumentException("betting 포인트 부족 or 차감 실패");
        }

        // 2) 환산 로직 (100원 = 1퐁)
        int pongAmount = amount / 100;

        // 3) pongdang 적립
        try {
            walletService.add(pongAmount, pongUserId, WalletType.PONG);
        } catch (RuntimeException e) {
            // 보상 트랜잭션: betting 되돌리기
            sUserRepository.addPoint(bettingUid, amount);
            throw e;
        }

        return pongAmount; // 전환된 퐁 단위 반환
    }

}
