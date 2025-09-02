package com.wepong.pongdang.dto.response;

import com.wepong.pongdang.entity.mapping.DonationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationResponseDTO {
    private Long id;
    private int amount;
    private Long userId;
    private String title;

    public static DonationResponseDTO from(DonationEntity donation) {
        return DonationResponseDTO.builder()
                .id(donation.getId())
                .amount(donation.getAmount())
                .userId(donation.getUser().getId())
                .title(donation.getDonationInfo().getTitle())
                .build();
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Status {
        private Long totalCount;
        private int totalAmount;
    }
}
