package com.wepong.pongdang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinanceResponseDTO {
    private int cluster;
    private String cluster_label;
    private double spend_rate;
    private double saving_rate;
    private double goal_achieve;
    private String strategy;
}
