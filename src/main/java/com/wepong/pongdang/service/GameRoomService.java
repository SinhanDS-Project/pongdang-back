package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.request.GameRoomRequestDTO;
import com.wepong.pongdang.dto.response.ChatResponseDTO;
import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.dto.response.WebSocketResponseDTO;
import com.wepong.pongdang.entity.GameEntity;
import com.wepong.pongdang.entity.GameLevelEntity;
import com.wepong.pongdang.entity.GameRoomEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.GameRoomStatus;
import com.wepong.pongdang.model.multi.turtle.PlayerDAO;
import com.wepong.pongdang.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GameRoomService {

	private final PlayerDAO playerDAO;
	private final GameRoomRepository gameRoomRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final AuthService authService;
	private final GameService gameService;
	private final GameLevelService gameLevelService;

	public GameRoomResponseDTO.GameRoomListDTO selectAll(int page) {
		int size = 6;
		PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
		Page<GameRoomEntity> roomlist = gameRoomRepository.findAll(pageRequest);

		Page<GameRoomResponseDTO.GameRoomDetailDTO> details = roomlist
				.map(room -> {
					GameLevelEntity level = gameLevelService.selectByLevelUid(room.getGameLevel().getId());
					GameEntity gameEntity = level != null ? gameService.selectById(level.getGame().getId()) : null;
					int count = playerDAO.getAll(room.getId()) != null ? playerDAO.getAll(room.getId()).size() : 0;

					return GameRoomResponseDTO.GameRoomDetailDTO.from(room, count);
				});

		return GameRoomResponseDTO.GameRoomListDTO.from(details);
	}

	public List<GameRoomEntity> selectAll() {
		return gameRoomRepository.findAll();
	}

	public GameRoomResponseDTO.GameRoomDetailDTO selectById(Long roomId) {
		GameRoomEntity room = gameRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("해당 게임방이 존재하지 않습니다"));

		if (room != null) {
			GameLevelEntity level = gameLevelService.selectByLevelUid(room.getGameLevel().getId());
			if (level != null) {
				GameEntity gameEntity = gameService.selectById(level.getGame().getId());
				if (gameEntity != null) {
					int count = playerDAO.getAll(room.getId()) != null
							? playerDAO.getAll(room.getId()).size()
							: 0;

					return GameRoomResponseDTO.GameRoomDetailDTO.from(room, count);
				}
			}
		}
		return null;
	}

	public void insertRoom(GameRoomRequestDTO.InsertGameRoomRequestDTO roomRequest, Long userId) {
		UserEntity userEntity = authService.findById(userId);
		GameLevelEntity level = gameLevelService.selectByLevelUid(roomRequest.getGameLevelId());

		GameRoomEntity room = GameRoomEntity.builder()
				.title(roomRequest.getTitle())
				.user(userEntity)
				.gameLevel(level)
				.status(GameRoomStatus.WAITING)
				.build();

		gameRoomRepository.save(room);
	}

	public void deleteRoom(Long roomId) {
		GameRoomEntity room = gameRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("해당 게임방이 존재하지 않습니다"));
		// 게임방 존재 여부
		if (room != null) {
			gameRoomRepository.delete(room);
		}
	}

	public void updateStatus(Long roomId, GameRoomStatus status) {
		GameRoomEntity room = gameRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("해당 게임방이 존재하지 않습니다"));
		room.updateStatus(status);

		gameRoomRepository.save(room);
	}

	public void updateHost(Long roomId, Long hostId) {
		GameRoomEntity room = gameRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("해당 게임방이 존재하지 않습니다"));
		UserEntity userEntity = authService.findById(hostId);

		room.updateHost(userEntity);

		gameRoomRepository.save(room);
	}

	public void sendRoom(Long roomId, String type, Object data) {
		WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
				.type(type)
				.data(data)
				.build();

		messagingTemplate.convertAndSend("/topic/gameroom/" + roomId, payload);
	}

	public void sendList(String type, Object data) {
		WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
				.type(type)
				.data(data)
				.build();

		messagingTemplate.convertAndSend("/topic/gameroom", payload);
	}

	public void sendGame(Long roomId, String type, Object data) {
		WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
				.type(type)
				.data(data)
				.build();

		messagingTemplate.convertAndSend("/topic/game" + roomId, payload);
	}

	public void sendGame(Long roomId, String type) {
		WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
				.type(type)
				.build();

		messagingTemplate.convertAndSend("/topic/game" + roomId, payload);
	}

	public void sendTest(String type, ChatResponseDTO chat) {
		WebSocketResponseDTO payload = WebSocketResponseDTO.builder()
				.type(type)
				.data(chat)
				.build();

		messagingTemplate.convertAndSend("/topic/test", payload);
	}
}
