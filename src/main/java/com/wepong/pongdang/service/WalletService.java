package com.wepong.pongdang.service;

import com.wepong.pongdang.exception.InsufficientBalanceException;
import org.springframework.stereotype.Service;

import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.WalletEntity;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.repository.WalletRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WalletService {

	private final WalletRepository walletRepository;

	public WalletEntity findByIdAndType(Long userId, WalletType type) {
		return walletRepository.findByUserIdAndWalletType(userId, type);
	}

	public void insertWallet(UserEntity user) {
		WalletEntity pongWallet = WalletEntity.builder()
			.walletType(WalletType.PONG)
			.user(user)
            .pongBalance(0L)
            .build();

		WalletEntity donaWallet = WalletEntity.builder()
			.walletType(WalletType.DONA)
			.user(user)
            .pongBalance(0L)
			.build();

		walletRepository.save(pongWallet);
		walletRepository.save(donaWallet);
	}

	// 퐁 차감
	public void lose(int point, Long userId, WalletType type) {
		WalletEntity pongWallet = walletRepository.findByUserIdAndWalletType(userId, type);

		if(pongWallet.getPongBalance() < point) {
			throw new InsufficientBalanceException();
		}

		pongWallet.setPongBalance(pongWallet.getPongBalance() - point);
		walletRepository.save(pongWallet);
	}

	// 퐁 증가
	public void add(int point, Long userId, WalletType type) {
		WalletEntity pongWallet = walletRepository.findByUserIdAndWalletType(userId, type);
		pongWallet.setPongBalance(pongWallet.getPongBalance() + point);
		walletRepository.save(pongWallet);
	}
}
