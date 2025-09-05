package com.wepong.pongdang.dto.request;

import lombok.Data;

@Data
public class TransferRequestDTO {
    private String uid;                // BettingPoint user UID
    private Integer transferAmount;    // 전환할 금액
    private UserRegisterDTO userRegister;
}