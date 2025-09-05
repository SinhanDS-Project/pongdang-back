package com.wepong.pongdang.service;

import com.wepong.pongdang.config.AmazonS3Config;
import com.wepong.pongdang.dto.request.BoardRequestDTO.InsertBoardRequestDTO;
import com.wepong.pongdang.dto.request.BoardRequestDTO.UpdateBoardRequestDTO;
import com.wepong.pongdang.dto.response.BoardResponseDTO;
import com.wepong.pongdang.entity.BoardEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.BoardType;
import com.wepong.pongdang.exception.BoardNotFoundException;
import com.wepong.pongdang.exception.BoardUnauthorizedException;
import com.wepong.pongdang.model.aws.S3FileService;
import com.wepong.pongdang.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

	private final BoardRepository boardRepository;

	@Autowired
	private AuthService authService;
	@Autowired
    private S3FileService s3FileService;
    @Autowired
	private AmazonS3Config amazonS3Config;

	// 게시글 리스트 조회, 페이징 (카테고리별), 정렬 포함
	public BoardResponseDTO.BoardListDTO getBoards(int page, int limit, BoardType category, String sort) {
		int offset = (page - 1) * limit;

		Pageable pageable = PageRequest.of(offset / limit, limit, Sort.by(sort).descending());
		Page<BoardEntity> boards = boardRepository.findByCategory(category, pageable);

		Page<BoardResponseDTO.BoardDetailDTO> details = boards.map(BoardResponseDTO.BoardDetailDTO::from);

		return BoardResponseDTO.BoardListDTO.from(details, category);
	}

	// 게시글 리스트 조회, 페이징 (정렬 X)
	public Page<BoardEntity> getBoards(int offset, int limit, BoardType category) {
		Pageable pageable = PageRequest.of(offset / limit, limit);
		return boardRepository.findByCategory(category, pageable);
	}

	// FAQ: 페이징 X, 정렬 X
	public List<BoardEntity> getBoards(BoardType category) {
		return boardRepository.findByCategory(category);
	}

	// 게시글 상세 조회(entity반환)
	public BoardEntity getBoardByUid(Long id) {
		BoardEntity board = boardRepository.findById(id)
				.orElseThrow(BoardNotFoundException::new);
		return board;
	}

	// 게시글 상세 조회(DTO반환)
	public BoardResponseDTO.BoardDetailDTO getBoardDetail(Long boardId) {
		// 조회수 증가
		incrementViewCount(boardId);
		// 게시글 데이터 가져오기
		BoardEntity boardEntity = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
		return BoardResponseDTO.BoardDetailDTO.from(boardEntity);
	}

	// 게시글 등록
	public void insertBoard(InsertBoardRequestDTO dto, Long userId) {
		UserEntity userEntity = authService.findById(userId);

		BoardEntity boardEntity = BoardEntity.builder().title(dto.getTitle()).content(dto.getContent())
				.category(dto.getCategory()).user(userEntity).build();
		boardRepository.save(boardEntity);
	}

	// 게시글 수정 - 로그인한 본인만 수정 가능
	public void updateBoard(Long id, UpdateBoardRequestDTO dto, Long userId) {
		// 1. 기존 게시글 조회
		BoardEntity existing = boardRepository.findById(id).orElseThrow(BoardNotFoundException::new);
		UserEntity userEntity = authService.findById(userId);
		// 2. 작성자 검증
		if (!existing.getUser().getId().equals(userEntity.getId())) {
            throw new BoardUnauthorizedException();
		}
		// 3. 수정할 내용으로 객체 생성
		BoardEntity boardEntity = BoardEntity.builder().id(existing.getId()).title(dto.getTitle()).content(dto.getContent())
				.category(dto.getCategory()).user(userEntity) // 그대로 유지
				.build();
		// 4. DB 수정
		boardRepository.save(boardEntity);
	}

	// 게시글 삭제 - 로그인한 본인만 삭제 가능 (s3 연동)
	public void deleteBoard(Long boardId, Long userId) {
		// 1. 기존 게시글 조회
		BoardEntity existing = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
		UserEntity userEntity = authService.findById(userId);
		// 2. 작성자 검증
		if (!existing.getUser().getId().equals(userEntity.getId())) {
            throw new BoardUnauthorizedException();
		}
		// 3. 본문 HTML에서 이미지 URL 추출 → S3에서 삭제
		String html = existing.getContent();
		if (html != null && html.toLowerCase().contains("<img")) {
			// 대소문자 무시, img 태그의 src="..." 부분만 뽑는 정규식
			Pattern p = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(html);

			// S3 URL prefix 계산
			String prefix = "https://" + amazonS3Config.getBucketName() + ".s3." + amazonS3Config.getRegion() + ".amazonaws.com/";
			while (m.find()) {
				String imageUrl = m.group(1);
				if (imageUrl.startsWith(prefix)) {
					String key = imageUrl.substring(prefix.length());
					s3FileService.deleteObject(key);
				}
			}
		}
		// 4. DB 삭제
		boardRepository.delete(existing);
	}

	// 조회수
	public void incrementViewCount(Long boardId) {
		BoardEntity existing = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
		existing.incrementViewCount();
	}

	// 좋아요 수
	public void incrementLikeCount(Long boardId) {
		BoardEntity existing = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
		existing.incrementLikeCount();
	}

}
