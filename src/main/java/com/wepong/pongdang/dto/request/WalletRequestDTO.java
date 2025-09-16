package com.wepong.pongdang.dto.request;

import com.wepong.pongdang.entity.enums.WalletType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletRequestDTO {
    private int amount;
    private WalletType walletType;
}
