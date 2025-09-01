package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.DonationInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationInfoRepository extends JpaRepository<DonationInfoEntity, Long> {
}
