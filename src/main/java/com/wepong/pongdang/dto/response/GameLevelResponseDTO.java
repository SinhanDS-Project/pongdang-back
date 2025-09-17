package com.wepong.pongdang.dto.response;

import com.wepong.pongdang.entity.GameLevelEntity;
import com.wepong.pongdang.entity.enums.Level;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GameLevelResponseDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelListDTO {
        private List<LevelDetailDTO> levels;

        public static LevelListDTO from(List<LevelDetailDTO> levels) {
            return LevelListDTO.builder()
                    .levels(levels)
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelDetailDTO {
        private Long id;
        private Level level;
        private double entryFee;
        private Long gameId;

        public static LevelDetailDTO from(GameLevelEntity level) {
            return LevelDetailDTO.builder()
                    .id(level.getId())
                    .level(level.getLevel())
                    .entryFee(level.getEntryFee())
                    .gameId(level.getGame().getId())
                    .build();
        }
    }
}
