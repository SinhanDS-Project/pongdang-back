package net.wepong.mysql.service;

import com.wepong.pongdang.exception.BettingUserNotFoundException;
import com.wepong.pongdang.exception.InsufficientPointException;
import com.wepong.pongdang.exception.UserCannotFoundException;
import lombok.RequiredArgsConstructor;
import net.wepong.mysql.entity.UserEntity;
import net.wepong.mysql.repository.SUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BettingUserPointService {

    private final SUserRepository sUserRepository;

    @Transactional
    public void deductPoint(String uid, int amount) {
        UserEntity user = sUserRepository.findById(uid)
                .orElseThrow(BettingUserNotFoundException::new);

        if (user.getPointBalance() < amount) {
            throw new InsufficientPointException();
        }

        user.setPointBalance(user.getPointBalance() - amount);
    }

    @Transactional
    public void addPoint(String uid, int amount) {
        UserEntity user = sUserRepository.findById(uid)
                .orElseThrow(UserCannotFoundException::new);

        user.setPointBalance(user.getPointBalance() + amount);
    }
}
