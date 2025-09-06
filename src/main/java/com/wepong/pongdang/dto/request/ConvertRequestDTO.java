package com.wepong.pongdang.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ConvertRequestDTO {
    private String name;
    private String phone;
    private int amount; // 전환할 포인트
}