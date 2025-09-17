package com.wepong.pongdang.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomRequestDTO {
	private String title;
	private Long gameLevelId;
}
