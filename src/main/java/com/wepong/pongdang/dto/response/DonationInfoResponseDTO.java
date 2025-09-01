package com.wepong.pongdang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DonationInfoResponseDTO {
    private Long id;
    private String title;
    private String purpose;
    private String content;
    private String org;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String type;
    private Long goal;
    private Long current;
    private String img;
}
