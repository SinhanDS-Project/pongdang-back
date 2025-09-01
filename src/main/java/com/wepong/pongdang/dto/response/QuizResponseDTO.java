package com.wepong.pongdang.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

public class QuizResponseDTO {

    /** LLM이 반환한 JSON을 파싱할 DTO */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class GenerateResponse {
        private List<GeneratedQuestion> questions;
    }

    /** LLM 1문항 스키마 (answer_idx → answerIdx 매핑) */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class GeneratedQuestion {
        private Integer position;      // 1~3
        private String question;
        private String choice1;
        private String choice2;
        private String choice3;
        private String choice4;
        @JsonProperty("answer_idx")
        private Integer answerIdx;     // 0~3
        private String explanation;
    }

    /** API로 내려줄 뷰 DTO */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class QuizView {
        private Integer position;
        private String question;
        private String choice1;
        private String choice2;
        private String choice3;
        private String choice4;
        private Integer answerIdx;
        private String explanation;
    }
}

