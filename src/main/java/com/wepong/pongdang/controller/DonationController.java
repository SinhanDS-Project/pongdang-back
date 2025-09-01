package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.DonationRequestDTO;
import com.wepong.pongdang.dto.response.DonationInfoResponseDTO;
import com.wepong.pongdang.dto.response.DonationResponseDTO;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donation")
public class DonationController {

    private final DonationService donationService;
    private final AuthService authService;

    // 기부 정보 리스트 조회
    @GetMapping("")
    public Page<DonationInfoResponseDTO> findAll(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return donationService.findInfoAll(page, size);
    }

    @GetMapping("/{infoId}")
    // 기부 정보 상세 조회
    public DonationInfoResponseDTO findById(@PathVariable Long infoId) {
        return donationService.findInfoById(infoId);
    }

    @PostMapping("")
    public DonationResponseDTO donate(@RequestBody DonationRequestDTO donationRequestDTO,
                                      @RequestHeader("Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return donationService.pongDonate(donationRequestDTO, userId);
    }

    @GetMapping("/status")
    public DonationResponseDTO.Status status() {
        return donationService.status();
    }

    @GetMapping("/history")
    public Page<DonationResponseDTO> findByUserId(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestHeader("Authorization") String authHeader) {
        Long userId = authService.validateAndGetUserId(authHeader);
        return donationService.findDonationByUserId(page, size, userId);
    }
}
