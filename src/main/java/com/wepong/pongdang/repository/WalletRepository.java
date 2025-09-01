package com.wepong.pongdang.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wepong.pongdang.entity.WalletEntity;
import com.wepong.pongdang.entity.enums.WalletType;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, String> {
	WalletEntity findByUserIdAndWalletType(Long userId, WalletType type);
}
