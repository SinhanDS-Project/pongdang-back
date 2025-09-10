package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.request.BoardRequestDTO.InsertBoardRequestDTO;
import com.wepong.pongdang.dto.request.BoardRequestDTO.UpdateBoardRequestDTO;
import com.wepong.pongdang.dto.response.BoardResponseDTO;
import com.wepong.pongdang.entity.BoardEntity;
import com.wepong.pongdang.entity.enums.BoardType;
import com.wepong.pongdang.exception.UnauthorizedAccessException;
import com.wepong.pongdang.model.aws.S3FileService;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/board")
public class BoardRestController {

	@Autowired
	private BoardService boardService;
	@Autowired
	private AuthService authService;
	@Autowired
	private S3FileService s3FileService;
	

	// 게시글 리스트 조회, 페이징 (카테고리별)
	@GetMapping("")
	public BoardResponseDTO.BoardListDTO list(
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "size", defaultValue = "5") int size,
			@RequestParam(value = "category", defaultValue = "FREE") BoardType category,
			@RequestParam(value = "sort", defaultValue = "createdAt") String sort) {

		return boardService.getBoards(page, size, category, sort);
	}

	// 게시글 상세보기 시 조회수 증가
    @GetMapping("/{boardId}")
	public BoardResponseDTO.BoardDetailDTO getBoardDetail2(
			@PathVariable("boardId") Long boardId,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {
		if (authHeader == null || authHeader.isBlank()) {
			throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
		}
		authService.validateAndGetUserId(authHeader);
		return boardService.getBoardDetail(boardId);
	}

	// 게시글 등록 (로그인한 사용자만 가능)
    @PostMapping("")
	public ResponseEntity<?> insertBoard(
			@RequestBody InsertBoardRequestDTO dto,
			@RequestHeader(value="Authorization", required=false) String authHeader) {
		if (authHeader == null || authHeader.isBlank()) {
			throw new UnauthorizedAccessException(); // "로그인 후 이용이 가능한 서비스입니다"
		}
		Long userId = authService.validateAndGetUserId(authHeader);
		boardService.insertBoard(dto, userId);
		return ResponseEntity.ok(Map.of("message", "게시글 등록이 완료되었습니다."));
	}

	// 게시글 수정 (로그인 && 본인 글만 가능)
    @PutMapping("/{boardId}")
	public ResponseEntity<?> updateBoard(
			@PathVariable("boardId") Long boardId,
			@RequestBody UpdateBoardRequestDTO dto,
			@RequestHeader("Authorization") String authHeader) {
		Long userId = authService.validateAndGetUserId(authHeader);
		boardService.updateBoard(boardId, dto, userId);
		return ResponseEntity.ok(Map.of("message", "게시글 수정이 완료되었습니다."));
	}

	// 게시글 삭제 (로그인 && 본인 글만 가능)
    @DeleteMapping("/{boardId}")
	public ResponseEntity<?> deleteBoard(
			@PathVariable("boardId") Long boardId,
			@RequestHeader("Authorization") String authHeader) {
		Long userId = authService.validateAndGetUserId(authHeader);
		boardService.deleteBoard(boardId, userId);
		return ResponseEntity.ok(Map.of("message", "게시글 삭제가 완료되었습니다."));
	}

	// 좋아요 버튼 누를 시 호출
	@PostMapping("/like/{boardId}")
	public ResponseEntity<Void> incrementLike(@PathVariable("boardId") Long boardId) {
		boardService.incrementLikeCount(boardId);
		return ResponseEntity.ok().build();
	}

	// 이미지 업로드 (S3 연동)
	@PostMapping(value = "/image-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public Map<String, Object> uploadImage(@RequestPart("image") MultipartFile file) {
		Map<String, Object> response = new HashMap<>();

		try {
			String imageUrl = s3FileService.uploadFile(file); // URL을 바로 받음

			response.put("url", imageUrl);
			response.put("success", 1);
			response.put("message", "업로드 성공");
		} catch (Exception e) {
			response.put("success", 0);
			response.put("message", "업로드 실패");
		}

		return response;
	}
	
	 // 이미지 삭제시
	 @DeleteMapping("/image-delete")
	    public void deleteImage(@RequestBody Map<String, List<String>> body) {
	        List<String> urls = body.get("urls");

	        if (urls != null) {
	            for (String url : urls) {
	                s3FileService.deleteFileByUrl(url);
	            }
	        }
	    }
}


	
	


