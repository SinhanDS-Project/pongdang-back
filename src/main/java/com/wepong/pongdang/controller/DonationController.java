package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.DonationInfoResponseDTO;
import com.wepong.pongdang.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donation")
public class DonationController {

    private final DonationService donationService;

    // 기부 정보 리스트 조회
    @GetMapping("")
    public Page<DonationInfoResponseDTO> findAll(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return donationService.findAll(page, size);
    }

    @GetMapping("/{infoId}")
    // 기부 정보 상세 조회
    public DonationInfoResponseDTO findById(@PathVariable Long infoId) {
        return donationService.findById(infoId);
    }
}
