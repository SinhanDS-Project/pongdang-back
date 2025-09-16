package com.wepong.pongdang.dto.response;

import com.wepong.pongdang.entity.mapping.PurchaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseResponseDTO {
    private String id;
    private Long userId;
    private String name;
    private int price;
    private LocalDateTime createdAt;

    public static PurchaseResponseDTO from(PurchaseEntity purchase) {
        return PurchaseResponseDTO.builder()
                .id(purchase.getId())
                .price(purchase.getProduct().getPrice())
                .userId(purchase.getUser().getId())
                .name(purchase.getProduct().getName())
                .createdAt(purchase.getCreatedAt())
                .build();
    }
}
