package com.wepong.pongdang.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuizRequestDTO {

    @JsonProperty("correctCount")
    private Integer correctCount; // 맞은 개수 (0~3)
}
