package com.wepong.pongdang.dto.response;

import com.wepong.pongdang.entity.GameHistoryEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.RankType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public class HistoryResponseDTO {
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GameResponseDTO {
		private Page<GameDetailResponseDTO> histories;
		private int currentPage;
		private int pageSize;
		private long totalCount;
		private int totalPages;

		public static GameResponseDTO from(Page<GameDetailResponseDTO> details) {
			return GameResponseDTO.builder()
					.histories(details)
					.currentPage(details.getNumber() + 1)
					.pageSize(details.getSize())
					.totalCount(details.getTotalElements())
					.totalPages(details.getTotalPages())
					.build();
		}
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GameDetailResponseDTO {
		private Long id;
		private Long userId;
		private Long gameId;
		private String gameName;
		private int entryFee;
		private RankType gameResult;
		private int pongValue;
		private LocalDateTime createdAt;

		public static GameDetailResponseDTO from(GameHistoryEntity history) {
			return GameDetailResponseDTO.builder()
					.id(history.getId())
					.userId(history.getUser().getId())
					.gameId(history.getGame().getId())
					.gameName(history.getGame().getName())
					.entryFee(history.getEntryFee())
					.gameResult(history.getRank())
					.pongValue(history.getPongValue())
					.createdAt(history.getCreatedAt())
					.build();
		}
	}
	
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PointResponseDTO {
		private Page<PointDetailResponseDTO> histories;
		private int currentPage;
		private int pageSize;
		private long totalCount;
		private int totalPages;

		public static PointResponseDTO from(Page<PointDetailResponseDTO> details) {
			return PointResponseDTO.builder()
					.histories(details)
					.currentPage(details.getNumber() + 1)
					.pageSize(details.getSize())
					.totalCount(details.getTotalElements())
					.totalPages(details.getTotalPages())
					.build();
		}
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PointDetailResponseDTO {
		private Long id;
		private Long userId;
		private PongHistoryType pongHistoryType;
		private int amount;
		private LocalDateTime createdAt;

		public static PointDetailResponseDTO from(PongHistoryEntity history) {
			return PointDetailResponseDTO.builder()
					.id(history.getId())
					.userId(history.getUser().getId())
					.pongHistoryType(history.getType())
					.amount(history.getAmount())
					.createdAt(history.getCreatedAt())
					.build();
		}
	}
}
