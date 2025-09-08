package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.ChatLogRequestDTO;
import com.wepong.pongdang.dto.response.ChatLogResponseDTO;
import com.wepong.pongdang.entity.ChatLogsEntity;
import com.wepong.pongdang.exception.UnauthorizedAccessException;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.ChatLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/chatlog")
public class ChatLogRestController {

	@Autowired
	ChatLogService chatLogService;
	@Autowired
	AuthService authService;
	
	// ✅ 사용자 UID로 채팅 로그 전체 조회
    @GetMapping("")
    public ChatLogResponseDTO.ChatLogListDTO getLogsByUserWithPaging(
				@RequestHeader(value = "Authorization", required = false) String authHeader,
				@RequestParam(value = "page", defaultValue = "1") int page) {

		if (authHeader == null || authHeader.isBlank()) {
			throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
		}
		Long userId = authService.validateAndGetUserId(authHeader);

    	Page<ChatLogsEntity> list = chatLogService.selectByUser(userId, page);
    	int totalCount = chatLogService.chatlogCount(userId);
		Page<ChatLogResponseDTO.ChatLogDetailDTO> details = list.map(ChatLogResponseDTO.ChatLogDetailDTO::from);
    	return ChatLogResponseDTO.ChatLogListDTO.builder()
    			.logs(details)
    			.total(totalCount)
    			.build();
    }
    
    // ✅ UID로 채팅 로그 상세 조회
    @GetMapping("/{chatlog_id}")
    public ChatLogResponseDTO.ChatLogDetailDTO getLogByUid(@PathVariable("chatlog_id") Long chatlogId) {
        ChatLogsEntity chatLogsEntity = chatLogService.selectByUid(chatlogId);
		return ChatLogResponseDTO.ChatLogDetailDTO.from(chatLogsEntity);
    }

    // ✅ 채팅 로그 등록
    @PostMapping("")
    public ResponseEntity<?> insertChatLog(
			@RequestBody ChatLogRequestDTO chatlog,
			@RequestHeader(value = "Authorization", required = false) String authHeader) throws IOException {
		if (authHeader == null || authHeader.isBlank()) {
			throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
		}

		Long userId = authService.validateAndGetUserId(authHeader);
		
        chatLogService.insertChatLog(chatlog, userId);

		return ResponseEntity.ok(Map.of("message", "질문이 등록되었습니다."));
    }

    // ✅ 로그 삭제
    @DeleteMapping("/{chatlog_id}")
    public ResponseEntity<?> deleteLog(@PathVariable("chatlog_id") Long chatlogId) {
        chatLogService.deleteLog(chatlogId);

		return ResponseEntity.ok(Map.of("message","질문이 삭제되었습니다."));
    }
}
