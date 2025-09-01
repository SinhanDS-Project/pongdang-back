package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.GameRoomRequestDTO;
import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.entity.enums.GameRoomStatus;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.GameRoomService;
import com.wepong.pongdang.socket.GameRoomListWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/gameroom")
public class GameRoomRestController {

	@Autowired
	GameRoomService gameRoomService;
	@Autowired
	AuthService authService;
	@Autowired
	private GameRoomListWebSocket gameRoomListWebSocket;

	// 게임방 리스트 조회
	@GetMapping("/list")
	public GameRoomResponseDTO.GameRoomListDTO selectAll(@RequestParam(defaultValue = "1") int page) {
		return gameRoomService.selectAll(page);
	}

	// 게임방 상세 조회
	@GetMapping("/detail/{roomId}")
	public GameRoomResponseDTO.GameRoomDetailDTO selectById(@PathVariable Long roomId) {
		return gameRoomService.selectById(roomId);
	}

	// 게임방 생성
	@PostMapping(value = "/insert", produces = "text/plain;charset=utf-8")
	public ResponseEntity<?> insertRoom(@RequestBody GameRoomRequestDTO.InsertGameRoomRequestDTO roomRequest,
									 @RequestHeader("Authorization") String authHeader) throws IOException {
		Long userId = authService.validateAndGetUserId(authHeader);
		gameRoomService.insertRoom(roomRequest, userId);
		gameRoomListWebSocket.broadcastMessage("insert");

		return ResponseEntity.ok("게임방이 생성되었습니다.");
	}

	// 게임 시작
	@PostMapping("/start/{roomId}")
	public ResponseEntity<?> startGame(@PathVariable Long roomId, @RequestBody Map<String, String> request) throws IOException {
		GameRoomStatus newStatus = GameRoomStatus.valueOf(request.get("status"));
		GameRoomResponseDTO.GameRoomDetailDTO room = gameRoomService.selectById(roomId);
		if(!room.getStatus().equals(newStatus)) {
			gameRoomService.updateStatus(roomId, newStatus);
			gameRoomListWebSocket.broadcastMessage("update");
		}
		return ResponseEntity.ok("게임이 시작되었습니다.");
	}
}