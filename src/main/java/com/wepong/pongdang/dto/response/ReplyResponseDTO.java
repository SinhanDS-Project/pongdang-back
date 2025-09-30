package com.wepong.pongdang.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wepong.pongdang.entity.mapping.ReplyEntity;
import com.wepong.pongdang.model.aws.S3ImagePathDeserializer;
import com.wepong.pongdang.model.aws.S3ImageUrlSerializer;
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
    @JsonSerialize(using = S3ImageUrlSerializer.class)
    @JsonDeserialize(using = S3ImagePathDeserializer.class)
    private String profileImg;
    private String createdAt; // 작성일 (문자열로 변환해서 반환)

    public static ReplyResponseDTO from(ReplyEntity replyEntity) {
        return ReplyResponseDTO.builder()
                .id(replyEntity.getId())
                .content(replyEntity.getContent())
                .writer(replyEntity.getUser().getNickname())
                .profileImg(replyEntity.getUser().getProfileImage())
                .createdAt(String.valueOf(replyEntity.getCreatedAt()))
                .build();
    }
}
