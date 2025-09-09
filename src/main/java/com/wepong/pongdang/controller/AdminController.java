package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.BannerRequestDTO;
import com.wepong.pongdang.dto.request.ChatLogRequestDTO;
import com.wepong.pongdang.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
