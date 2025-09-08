package com.wepong.pongdang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConvertResponseDTO {
    private String message;
    private Integer converted;
    private Integer bettingPointAfter;
    private Long pongBalanceAfter;
}
