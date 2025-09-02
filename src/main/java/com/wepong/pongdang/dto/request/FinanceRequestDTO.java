package com.wepong.pongdang.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinanceRequestDTO {
    private int age; // 나이
    private int income; // 월 소득
    private int spend; // 월 소비
    private String main_category; // 주요 소비 항목
    private int saving_goal; // 목표 저축액
    private int current_saving; // 현재 저축액
    private String loan; // 부채 여부
    private String invest_type; // 안정형, 중립형, 공격형
    private String goal_term; // 목표 기간
}
