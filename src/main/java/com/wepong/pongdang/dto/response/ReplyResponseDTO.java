package com.wepong.pongdang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReplyResponseDTO {
    private Long id;          // 댓글 PK
    private String content;   // 댓글 내용
    private String writer;    // 작성자 닉네임
    private String createdAt; // 작성일 (문자열로 변환해서 반환)
}
