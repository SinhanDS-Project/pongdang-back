package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.request.ReplyRequestDTO;
import com.wepong.pongdang.dto.response.ReplyResponseDTO;
import com.wepong.pongdang.entity.BoardEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.mapping.ReplyEntity;
import com.wepong.pongdang.exception.*;
import com.wepong.pongdang.repository.BoardRepository;
import com.wepong.pongdang.repository.ReplyRepository;
import com.wepong.pongdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    public ReplyResponseDTO addReply(Long boardId, Long userId, ReplyRequestDTO dto) {
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(BoardNotFoundException::new);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        ReplyEntity reply = ReplyEntity.builder()
                .board(board)
                .user(user)
                .content(dto.getContent())
                .build();

        ReplyEntity saved = replyRepository.save(reply);

        return ReplyResponseDTO.from(saved);
    }

    // 댓글 목록 조회
    public List<ReplyResponseDTO> getReplies(Long boardId) {
        return replyRepository.findByBoardId(boardId).stream()
                .map(r -> ReplyResponseDTO.from(r))
                .toList();
    }

    // 댓글 수정
    public ReplyResponseDTO updateReply(Long replyId, Long userId, ReplyRequestDTO dto) {
        ReplyEntity reply = replyRepository.findById(replyId)
                .orElseThrow(ReplyNotFoundException::new);

        // 본인 댓글만 수정 가능
        if (!reply.getUser().getId().equals(userId)) {
            throw new ReplyUnauthorizedException();
        }

        // 수정 내용 반영
        reply.setContent(dto.getContent());

        // save 호출 시 JPA가 dirty checking으로 업데이트 쿼리 실행
        ReplyEntity updated = replyRepository.save(reply);

        return ReplyResponseDTO.from(updated);
    }

    // 댓글삭제
    public void deleteReply(Long replyId, Long userId) {
        ReplyEntity reply = replyRepository.findById(replyId)
                .orElseThrow(ReplyNotFoundException::new);

        if (!reply.getUser().getId().equals(userId)) {
            throw new ReplyUnauthorizedException();
        }

        replyRepository.delete(reply);
    }
}
