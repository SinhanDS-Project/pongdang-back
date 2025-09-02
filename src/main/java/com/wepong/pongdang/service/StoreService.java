package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.request.PurchaseRequestDTO;
import com.wepong.pongdang.dto.response.ProductResponseDTO;
import com.wepong.pongdang.dto.response.PurchaseResponseDTO;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.ProductEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.ProductType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.entity.mapping.PurchaseEntity;
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
    private final AuthService authService;
    private final WalletService walletService;
    private final HistoryService historyService;
    ModelMapper modelMapper = new ModelMapper();

    // 상품 리스트 조회(페이징)
    public Page<ProductResponseDTO> findProducts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page-1, size, Sort.Direction.DESC, "createdAt");
        Page<ProductEntity> productList = productRepository.findAll(pageRequest);
        Page<ProductResponseDTO> responseList = productList.map(entity -> modelMapper.map(entity, ProductResponseDTO.class));

        return responseList;
    }

    // 카테고리 별 상품 조회(페이징)
    public Page<ProductResponseDTO> findProductByType(ProductType type, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page-1, size, Sort.Direction.DESC, "createdAt");
        Page<ProductEntity> productList = productRepository.findByType(pageRequest, type);
        Page<ProductResponseDTO> responseList = productList.map(entity -> modelMapper.map(entity, ProductResponseDTO.class));

        return responseList;
    }

    // 상품 상세 조회
    public ProductResponseDTO findProductById(Long productId) {
        ProductEntity entity = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));
        return modelMapper.map(entity, ProductResponseDTO.class);
    }

    // 상품 구매
    public PurchaseResponseDTO purchase(PurchaseRequestDTO request, Long userId) {
        ProductEntity product = productRepository.findById(request.getProductId()).orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));
        UserEntity user = authService.findById(userId);

        // 일반 퐁 차감
        walletService.lose(request.getPrice(), userId, WalletType.PONG);

        // 퐁 내역 저장
        PongHistoryEntity pongHistory = PongHistoryEntity.builder()
                .amount(request.getPrice())
                .type(PongHistoryType.PURCHASE)
                .user(user)
                .build();

        historyService.insertPointHistory(pongHistory, userId);

        // 상품 구매 내역 저장
        PurchaseEntity purchase = PurchaseEntity.builder()
                .product(product)
                .user(user)
                .build();

        purchaseRepository.save(purchase);

        return PurchaseResponseDTO.from(purchase);
    }

    // 사용자 구매 내역 조회
    public Page<PurchaseResponseDTO> findPurchaseByUserId(int page, int size, Long userId) {
        PageRequest pageRequest = PageRequest.of(page-1, size, Sort.Direction.DESC, "createdAt");
        Page<PurchaseEntity> purchaseList = purchaseRepository.findByUserId(userId, pageRequest);
        Page<PurchaseResponseDTO> responseList = purchaseList.map(entity -> PurchaseResponseDTO.from(entity));

        return responseList;
    }

    // 상품 검색
    public Page<ProductResponseDTO> searchProducts(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page-1, size, Sort.Direction.DESC, "createdAt");
        Page<ProductEntity> productList = productRepository.findByNameContaining(pageRequest, keyword);
        Page<ProductResponseDTO> responseList = productList.map(entity -> modelMapper.map(entity, ProductResponseDTO.class));

        return responseList;
    }
}
