package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.DonationInfoResponseDTO;
import com.wepong.pongdang.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donation")
public class DonationController {

    private final DonationService donationService;

    // 기부 정보 리스트 조회
    @GetMapping("/")
    public List<DonationInfoResponseDTO> findAll() {
        return donationService.findAll();
    }

    @GetMapping("/{infoId}")
    // 기부 정보 상세 조회
    public DonationInfoResponseDTO findById(@PathVariable Long infoId) {
        return donationService.findById(infoId);
    }
}
