package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.GameRoomRequestDTO;
import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.entity.enums.GameRoomStatus;
import com.wepong.pongdang.exception.UnauthorizedAccessException;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.GameRoomService;
import com.wepong.pongdang.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gameroom")
@RequiredArgsConstructor
public class GameRoomRestController {

	private final GameRoomService gameRoomService;
	private final AuthService authService;
	private final WebSocketService webSocketService;

	// 게임방 리스트 조회
	@GetMapping("")
	public GameRoomResponseDTO.GameRoomListDTO selectAll(@RequestParam(defaultValue = "1") int page) {
		return gameRoomService.selectAll(page);
	}

	// 게임방 상세 조회
	@GetMapping("/{roomId}")
	public GameRoomResponseDTO.GameRoomDetailDTO selectById(@PathVariable Long roomId) {
		return gameRoomService.selectById(roomId);
	}

	// 게임방 생성
	@PostMapping(value = "")
	public ResponseEntity<?> insertRoom(@RequestBody GameRoomRequestDTO roomRequest,
									 @RequestHeader(value = "Authorization", required = false) String authHeader) {
		if (authHeader == null || authHeader.isBlank()) {
			throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
		}
		Long userId = authService.validateAndGetUserId(authHeader);
		gameRoomService.insertRoom(roomRequest, userId);
		webSocketService.sendList(gameRoomService.selectAll());

		return ResponseEntity.ok(Map.of("message", "게임방이 생성되었습니다."));
	}

	// 게임 시작
	@PostMapping("/start/{roomId}")
	public ResponseEntity<?> startGame(@PathVariable Long roomId, @RequestBody Map<String, String> request) {
		GameRoomStatus newStatus = GameRoomStatus.valueOf(request.get("status"));
		GameRoomResponseDTO.GameRoomDetailDTO room = gameRoomService.selectById(roomId);
		if(!room.getStatus().equals(newStatus)) {
			gameRoomService.updateStatus(roomId, newStatus);
			webSocketService.sendList(gameRoomService.selectAll());
		}
		return ResponseEntity.ok(Map.of("message", "게임이 시작되었습니다."));
	}
}