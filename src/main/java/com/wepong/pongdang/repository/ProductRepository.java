package com.wepong.pongdang.repository;

import com.wepong.pongdang.entity.ProductEntity;
import com.wepong.pongdang.entity.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Page<ProductEntity> findByType(PageRequest pageRequest, ProductType type);

    Page<ProductEntity> findByNameContaining(PageRequest pageRequest, String keyword);
}
