package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.ReplyRequestDTO;
import com.wepong.pongdang.dto.response.ReplyResponseDTO;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/board/{boardId}/replies")
@RequiredArgsConstructor
public class ReplyRestController {

    private final ReplyService replyService;
    private final AuthService authService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<ReplyResponseDTO> addReply(
            @PathVariable Long boardId,
            @RequestBody ReplyRequestDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authService.validateAndGetUserId(authHeader);
        ReplyResponseDTO reply = replyService.addReply(boardId, userId, dto);
        return ResponseEntity.ok(reply);
    }

    // 댓글 목록 조회
    @GetMapping
    public ResponseEntity<List<ReplyResponseDTO>> getReplies(@PathVariable Long boardId) {
        return ResponseEntity.ok(replyService.getReplies(boardId));
    }

    // 댓글 수정
    @PutMapping("/{replyId}")
    public ResponseEntity<ReplyResponseDTO> updateReply(
            @PathVariable Long boardId,
            @PathVariable Long replyId,
            @RequestBody ReplyRequestDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authService.validateAndGetUserId(authHeader);
        ReplyResponseDTO updated = replyService.updateReply(replyId, userId, dto);
        return ResponseEntity.ok(updated);
    }


    // 댓글 삭제
    @DeleteMapping("/{replyId}")
    public ResponseEntity<String> deleteReply(
            @PathVariable Long boardId,
            @PathVariable Long replyId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = authService.validateAndGetUserId(authHeader);
        replyService.deleteReply(replyId, userId);
        return ResponseEntity.ok("댓글 삭제 완료");
    }
}
