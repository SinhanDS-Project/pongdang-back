package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.BannerRequestDTO;
import com.wepong.pongdang.dto.request.ChatLogRequestDTO;
import com.wepong.pongdang.service.AdminService;
import com.wepong.pongdang.dto.request.ProductRequestDTO;
import com.wepong.pongdang.dto.response.DonationInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import org.springframework.http.MediaType;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    // 배너 등록
    @PostMapping(
            value = "/banner",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"
    )
    public ResponseEntity<?> insertBanner(@RequestPart("banner") BannerRequestDTO banner,
                                       @RequestPart("file") MultipartFile file) {
        adminService.insertBanner(banner, file);
        return ResponseEntity.ok(Map.of("message", "배너 등록 성공"));
    }

    // 1:1 문의 내역 전체 조회
    @GetMapping("/chatlogs")
    public ResponseEntity<?> getChatlogs() {
        return ResponseEntity.ok(adminService.selectAllChatLogs());
    }

    // 1:1 문의 답변
    @PutMapping("/chatlogs/{id}")
    public ResponseEntity<?> answerChatLog(@PathVariable("id") Long id,
                                           @RequestBody ChatLogRequestDTO.ChatLogAnswerRequestDTO request) {
        adminService.insertChatLogAnswer(id, request);
        return ResponseEntity.ok(Map.of("message", "답변이 등록/수정되었습니다."));
    }

    // 상품 등록
    @PostMapping(value = "/product",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> saveProduct(@RequestBody ProductRequestDTO request,
                                         @RequestPart("files") MultipartFile[] files) {
        adminService.saveProduct(request, files);
        return ResponseEntity.ok(Map.of("message", "상품이 등록되었습니다."));
    }

    // 기부 전체 조회
    @GetMapping("/donation")
    public List<DonationInfoResponseDTO> findInfoAll() {
        return adminService.findInfoAll();
    }

    // 기부 정보 수정
    @PutMapping(value = "/donation/{infoId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDonationInfo(@PathVariable Long infoId, @RequestPart("file") MultipartFile file) {
        adminService.updateDonationInfo(infoId, file);
        return ResponseEntity.ok(Map.of("message", "기부 정보가 수정되었습니다."));
    }
}
