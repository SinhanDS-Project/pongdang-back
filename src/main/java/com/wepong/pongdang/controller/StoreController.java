package com.wepong.pongdang.controller;

import com.google.zxing.WriterException;
import com.wepong.pongdang.dto.request.PurchaseRequestDTO;
import com.wepong.pongdang.dto.response.ProductResponseDTO;
import com.wepong.pongdang.dto.response.PurchaseResponseDTO;
import com.wepong.pongdang.entity.enums.ProductType;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.BarcodeService;
import com.wepong.pongdang.service.StoreService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreController {

    private final AuthService authService;
    private final BarcodeService barcodeService;
    private final StoreService storeService;

    // 상품 리스트 조회
    @GetMapping("/product")
    public Page<ProductResponseDTO> findProducts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return storeService.findProducts(page, size);
    }

    // 카테고리 별 조회
    @GetMapping("/product/category")
    public Page<ProductResponseDTO> findProductByType(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam ProductType type) {
        return storeService.findProductByType(type, page, size);
    }

    // 상품 검색
    @GetMapping("/product/search")
    public Page<ProductResponseDTO> searchProducts(@RequestParam String keyword,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return storeService.searchProducts(keyword, page, size);
    }

    // 상품 상세 조회
    @GetMapping("product/{productId}")
    public ProductResponseDTO findProduct(@PathVariable Long productId) {
        return storeService.findProductById(productId);
    }

    // 상품 구매
    @PostMapping("/purchase")
    public PurchaseResponseDTO purchase(@RequestBody PurchaseRequestDTO purchaseRequestDTO,
                                        @RequestHeader("Authorization") String authHeader) throws MessagingException, IOException, WriterException {
        Long userId = authService.validateAndGetUserId(authHeader);
        barcodeService.generateBarcode(userId, purchaseRequestDTO.getProductId());
        return storeService.purchase(purchaseRequestDTO, userId);
    }

    // 사용자 구매 내역 조회
    @GetMapping("/history")
    public Page<PurchaseResponseDTO> findPurchaseByUserId(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestHeader("Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return storeService.findPurchaseByUserId(page, size, userId);
    }
}
