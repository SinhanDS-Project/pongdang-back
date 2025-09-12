package com.wepong.pongdang.model.multi.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LandDTO {
    private Integer landId; // (1~24)
    private String name;
    private int price; // 가격
    private int toll; // 통행료
    private Long ownerId; // null이면 무소유
    private String color; // = turtleId
}
