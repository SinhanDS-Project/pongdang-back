package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.mapping.PurchaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {
    Page<PurchaseEntity> findByUserId(Long userId, PageRequest pageRequest);
}
