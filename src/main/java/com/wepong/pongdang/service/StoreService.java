package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.response.ProductResponseDTO;
import com.wepong.pongdang.entity.ProductEntity;
import com.wepong.pongdang.repository.ProductRepository;
import com.wepong.pongdang.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    ModelMapper modelMapper = new ModelMapper();

    // 상품 리스트 조회(페이징)
    public Page<ProductResponseDTO> findProducts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page-1, size, Sort.Direction.DESC, "createdAt");
        Page<ProductEntity> productList = productRepository.findAll(pageRequest);
        Page<ProductResponseDTO> responseList = productList.map(entity -> modelMapper.map(entity, ProductResponseDTO.class));

        return responseList;
    }

    // 상품 상세 조회
    public ProductResponseDTO findProductById(Long productId) {
        ProductEntity entity = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));
        return modelMapper.map(entity, ProductResponseDTO.class);
    }

    // 상품 구매

    // 사용자 구매 내역 조회
}
