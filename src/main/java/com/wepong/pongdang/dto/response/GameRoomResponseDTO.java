package com.wepong.pongdang.dto.response;

import com.wepong.pongdang.entity.GameRoomEntity;
import com.wepong.pongdang.entity.enums.GameRoomStatus;
import com.wepong.pongdang.entity.enums.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public class GameRoomResponseDTO {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GameRoomListDTO {
		private Page<GameRoomDetailDTO> gameRooms;
		private int currentPage;
		private int totalPages;

		public static GameRoomListDTO from(Page<GameRoomDetailDTO> gameRooms) {
			return GameRoomListDTO.builder()
					.gameRooms(gameRooms)
					.currentPage(gameRooms.getNumber() + 1)
					.totalPages(gameRooms.getTotalPages())
					.build();
		}
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GameRoomDetailDTO {
		private Long id;
		private String title;
		private int entryFee;
		private GameRoomStatus status;
		private LocalDateTime createdAt;
		private LocalDateTime startAt;
		private Long hostId;
		private Long gameLevelId;
		private Level level;
		private String gameName;
		private int count;

		public static GameRoomDetailDTO from(GameRoomEntity room, int count) {
			return GameRoomDetailDTO.builder()
					.id(room.getId())
					.title(room.getTitle())
					.gameLevelId(room.getGameLevel().getId())
					.level(room.getGameLevel().getLevel())
					.gameName(room.getGameLevel().getGame().getName())
					.count(count)
					.entryFee(room.getGameLevel().getEntryFee())
					.hostId(room.getUser().getId())
					.createdAt(room.getCreatedAt())
					.status(room.getStatus())
					.build();
		}

		public static GameRoomDetailDTO from(GameRoomEntity room) {
			return GameRoomDetailDTO.builder()
					.id(room.getId())
					.title(room.getTitle())
					.gameLevelId(room.getGameLevel().getId())
					.level(room.getGameLevel().getLevel())
					.gameName(room.getGameLevel().getGame().getName())
					.entryFee(room.getGameLevel().getEntryFee())
					.hostId(room.getUser().getId())
					.createdAt(room.getCreatedAt())
					.status(room.getStatus())
					.build();
		}
	}
}
