package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.DonationResponseDTO;
import com.wepong.pongdang.dto.response.HistoryResponseDTO;
import com.wepong.pongdang.dto.response.PurchaseResponseDTO;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.DonationService;
import com.wepong.pongdang.service.HistoryService;
import com.wepong.pongdang.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryRestController {

    private final HistoryService historyService;
    private final AuthService authService;
    private final StoreService storeService;
    private final DonationService donationService;

    @GetMapping("/game/list")
    public HistoryResponseDTO.GameResponseDTO gameHistoryList(
    											@RequestHeader("Authorization") String authHeader,
                                                @RequestParam(defaultValue = "1") int page) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return historyService.gameHistoryList(userId, page);
    }

    // 사용자 구매 내역 조회
    @GetMapping("/purchase")
    public Page<PurchaseResponseDTO> findPurchaseByUserId(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestHeader("Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return storeService.findPurchaseByUserId(page, size, userId);
    }

    // 사용자 기부 내역 조회
    @GetMapping("/donation")
    public Page<DonationResponseDTO> findByUserId(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestHeader("Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return donationService.findDonationByUserId(page, size, userId);
    }

    // 사용자 퐁 내역 조회
    @GetMapping("/wallet")
    public HistoryResponseDTO.PointResponseDTO pointHistoryList(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return historyService.pointHistoryList(userId, page, size);
    }
}