package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.request.ReplyRequestDTO;
import com.wepong.pongdang.dto.response.ReplyResponseDTO;
import com.wepong.pongdang.entity.BoardEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.mapping.ReplyEntity;
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
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        ReplyEntity reply = ReplyEntity.builder()
                .board(board)
                .user(user)
                .content(dto.getContent())
                .build();

        ReplyEntity saved = replyRepository.save(reply);

        return new ReplyResponseDTO(
                saved.getId(),
                saved.getContent(),
                user.getNickname(),
                saved.getCreatedAt().toString()
        );
    }

    // 댓글 목록 조회
    public List<ReplyResponseDTO> getReplies(Long boardId) {
        return replyRepository.findByBoardId(boardId).stream()
                .map(r -> new ReplyResponseDTO(
                        r.getId(),
                        r.getContent(),
                        r.getUser().getNickname(),
                        r.getCreatedAt().toString()
                ))
                .toList();
    }

    // 댓글삭제
    public void deleteReply(Long replyId, Long userId) {
        ReplyEntity reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다."));

        if (!reply.getUser().getId().equals(userId)) {
            throw new RuntimeException("본인 댓글만 삭제할 수 있습니다.");
        }

        replyRepository.delete(reply);
    }
}
