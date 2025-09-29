package com.wepong.pongdang.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wepong.pongdang.entity.BoardEntity;
import com.wepong.pongdang.entity.enums.BoardType;
import com.wepong.pongdang.model.aws.S3ImagePathDeserializer;
import com.wepong.pongdang.model.aws.S3ImageUrlSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

public class BoardResponseDTO {

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class BoardListDTO {
        /** 한 페이지에 담긴 게시글 목록 */
        private Page<BoardDetailDTO> boards;
        /** 현재 페이지 번호 (1부터 시작) */
        private int currentPage;
        /** 페이지당 게시글 수 */
        private int pageSize;
        /** 전체 게시글 건수 */
        private long totalCount;
        /** 전체 페이지 수 */
        private int totalPages;
        /** (선택) 요청한 카테고리 */
        private BoardType category;

        public static BoardListDTO from(Page<BoardDetailDTO> boards, BoardType category) {
            return BoardListDTO.builder()
                    .boards(boards)
                    .currentPage(boards.getNumber() + 1)
                    .pageSize(boards.getSize())
                    .totalCount(boards.getTotalElements())
                    .totalPages(boards.getTotalPages())
                    .category(category)
                    .build();
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class BoardDetailDTO {
        private Long id;
        private String title;
        private String content;
        private BoardType category;
        private int viewCount;
        private int likeCount;
        private int replyCount;
        private Long userId;
        @JsonSerialize(using = S3ImageUrlSerializer.class)
        @JsonDeserialize(using = S3ImagePathDeserializer.class)
        private String profileImg;
        private String nickname;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static BoardDetailDTO from(BoardEntity boardEntity) {
            return BoardDetailDTO.builder()
                .id(boardEntity.getId())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .category(boardEntity.getCategory())
                .viewCount(boardEntity.getViewCount())
                .likeCount(boardEntity.getLikeCount())
                .replyCount(boardEntity.getReplies().size())
                .userId(boardEntity.getUser().getId())
                .profileImg(boardEntity.getUser().getProfileImage())
                .nickname(boardEntity.getUser().getNickname())
                .createdAt(boardEntity.getCreatedAt())
                .updatedAt(boardEntity.getUpdatedAt())
                .build();
        }
    }
}
