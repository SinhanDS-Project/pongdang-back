package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.mapping.DonationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationRepository extends JpaRepository<DonationEntity, Long> {
    Page<DonationEntity> findByUserId(Long userId, PageRequest pageRequest);
}
