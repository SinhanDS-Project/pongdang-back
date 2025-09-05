package com.wepong.pongdang.controller;

import com.wepong.pongdang.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "USER_NOT_FOUND", "message", ex.getMessage()));
	}

	@ExceptionHandler(InvalidPasswordException.class)
	public ResponseEntity<?> handleInvalidPassword(InvalidPasswordException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "INVALID_PASSWORD", "message", ex.getMessage()));
	}

	@ExceptionHandler(MissingCredentialsException.class)
	public ResponseEntity<?> handleMissingCredentials(MissingCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "MISSING_CREDENTIALS", "message", ex.getMessage()));
	}

	@ExceptionHandler(SessionExpiredException.class)
	public ResponseEntity<?> handleSessionExpired(SessionExpiredException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "SESSION_EXPIRED", "message", ex.getMessage()));
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ResponseEntity<?> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "UNAUTHORIZED_ACCESS", "message", ex.getMessage()));
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<?> handleInvalidToken(InvalidTokenException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "INVALID_TOKEN", "message", ex.getMessage()));
	}
	
	@ExceptionHandler(InvalidUpdatePasswordException.class)
    public ResponseEntity<?> handleInvalidUpdatePassword(InvalidUpdatePasswordException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "INVALID_UPDATE_PASSWORD", "message", ex.getMessage()));
    }

	@ExceptionHandler(QuizAlreadyTakenException.class)
	public ResponseEntity<?> handleQuizAlreadyTaken(QuizAlreadyTakenException ex) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT) // 409 Conflict
				.body(Map.of("error", "ALREADY_TODAY_QUIZ_FINISHED", "message", ex.getMessage()));
	}

	@ExceptionHandler(BoardNotFoundException.class)
	public ResponseEntity<?> handleBoardNotFound(BoardNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", "BOARD_NOT_FOUND", "message", ex.getMessage()));
	}

	@ExceptionHandler(BoardUnauthorizedException.class)
	public ResponseEntity<?> handleBoardUnauthorized(BoardUnauthorizedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "BOARD_UNAUTHORIZED", "message", ex.getMessage()));
	}

	@ExceptionHandler(ReplyNotFoundException.class)
	public ResponseEntity<?> handleReplyNotFound(ReplyNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", "REPLY_NOT_FOUND", "message", ex.getMessage()));
	}

	@ExceptionHandler(ReplyUnauthorizedException.class)
	public ResponseEntity<?> handleReplyUnauthorized(ReplyUnauthorizedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("error", "REPLY_UNAUTHORIZED", "message", ex.getMessage()));
	}

	@ExceptionHandler(AlreadyAttendanceException.class)
	public ResponseEntity<?> handleAttendanceAlreadyTaken(AlreadyAttendanceException ex) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT) // 409 Conflict
				.body(Map.of("error", "ALREADY_ATTENDANCE_FINISHED", "message", ex.getMessage()));
	}

	@ExceptionHandler(UserCannotFoundException.class)
	public ResponseEntity<?> handleUserCannotFound(UserCannotFoundException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "USER_CANNOT_FOUND", "message", ex.getMessage()));
	}

	// Optional: 모든 예외를 처리하는 fallback
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleAll(Exception ex) {
		ex.printStackTrace();
		String message = ex.getMessage() != null ? ex.getMessage() : "알 수 없는 오류";
		String detail = ex.getCause() != null && ex.getCause().getMessage() != null ? ex.getCause().getMessage()
				: "원인 정보 없음";

		Map<String, Object> body = Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.value(), "message", message, "detail",
				detail);
		
		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
