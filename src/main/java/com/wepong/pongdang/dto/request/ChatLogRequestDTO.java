package com.wepong.pongdang.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public class ChatLogRequestDTO {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatLogQuestionDTO {
        private String title;     // 문의 제목
        private String question;  // 문의 내용
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatLogAnswerRequestDTO {
        private String response;          // 답변 내용
    }
}
