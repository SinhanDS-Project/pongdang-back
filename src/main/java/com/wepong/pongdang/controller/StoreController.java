package com.wepong.pongdang.controller;

import com.google.zxing.WriterException;
import com.wepong.pongdang.dto.response.ProductResponseDTO;
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
    public Page<ProductResponseDTO> findProducts(@RequestParam int page, @RequestParam int size) {
        return storeService.findProducts(page, size);
    }

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ProductResponseDTO findProduct(@PathVariable Long productId) {
        return storeService.findProductById(productId);
    }
}
