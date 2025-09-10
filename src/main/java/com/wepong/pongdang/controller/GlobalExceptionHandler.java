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

	@ExceptionHandler(FinanceReportException.class)
	public ResponseEntity<?> handleFinanceReport(FinanceReportException ex) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
				.body(Map.of("error", "FINANCE_REPORT_NOT_GENERATED", "message", ex.getMessage()));
	}

	@ExceptionHandler(QuizNotGeneratedException.class)
	public ResponseEntity<?> handleQuizNotGenerated(QuizNotGeneratedException ex) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND) // 404 Not Found
				.body(Map.of("error", "QUIZ_NOT_GENERATED", "message", ex.getMessage()));
	}

	@ExceptionHandler(UserCannotFoundException.class)
	public ResponseEntity<?> handleUserCannotFound(UserCannotFoundException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "USER_CANNOT_FOUND", "message", ex.getMessage()));
	}

	// 기부 정보가 존재하지 않음
	@ExceptionHandler(DonationNotFoundException.class)
	public ResponseEntity<?> handleDonationNotFound(DonationNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", "DONATION_NOT_FOUND", "message", ex.getMessage()));
	}

	// 상품이 존재하지 않음
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<?> handleProductNotFound(ProductNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", "PRODUCT_NOT_FOUND", "message", ex.getMessage()));
	}

    // BettingUser 관련 예외 처리
    @ExceptionHandler(BettingUserNotFoundException.class)
    public ResponseEntity<?> handleBettingUserNotFound(BettingUserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "BETTINGUSER_NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientPointException.class)
    public ResponseEntity<?> handleInsufficientPoint(InsufficientPointException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "INSUFFICIENT_POINT", "message", ex.getMessage()));
    }

	// 게임이 존재하지 않음
	@ExceptionHandler(GameNotFoundException.class)
	public ResponseEntity<?> handleGameNotFound(GameNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", "GAME_NOT_FOUND", "message", ex.getMessage()));
	}

	// 게임방이 존재하지 않음
	@ExceptionHandler(RoomNotFoundException.class)
	public ResponseEntity<?> handleRoomNotFound(RoomNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(Map.of("error", "ROOM_NOT_FOUND", "message", ex.getMessage()));
	}

	// 잔액이 부족함
	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<?> handleInsufficientBalance(InsufficientBalanceException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "INSUFFICIENT_BALANCE", "message", ex.getMessage()));
	}

	// 닉네임 중복
	@ExceptionHandler(NicknameAlreadyExist.class)
	public ResponseEntity<?> handleNicknameAlready(NicknameAlreadyExist ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "NICKNAME_ALREADY_EXIST", "message", ex.getMessage()));
	}

	// 필수 항목 누락
	@ExceptionHandler(MissingRequiredFieldsException.class)
	public ResponseEntity<?> handleMissingRequiredFields(MissingRequiredFieldsException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "MISSING_REQUIRED_FIELDS", "message", ex.getMessage()));
	}

	// 이메일 중복
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<?> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "EMAIL_ALREADY_EXISTS", "message", ex.getMessage()));
	}

	// 비밀번호 불일치
	@ExceptionHandler(PasswordMismatchException.class)
	public ResponseEntity<?> handlePasswordMismatch(PasswordMismatchException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "PASSWORD_MISMATCH", "message", ex.getMessage()));
	}

	// 비밀번호 형식 오류
	@ExceptionHandler(InvalidPasswordFormatException.class)
	public ResponseEntity<?> handleInvalidPasswordFormat(InvalidPasswordFormatException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "INVALID_PASSWORD_FORMAT", "message", ex.getMessage()));
	}

	// 전화번호 형식 오류
	@ExceptionHandler(InvalidPhoneNumberFormatException.class)
	public ResponseEntity<?> handleInvalidPhoneNumberFormat(InvalidPhoneNumberFormatException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "INVALID_PHONE_NUMBER_FORMAT", "message", ex.getMessage()));
	}

	// 만 15세 미만
	@ExceptionHandler(UnderAgeException.class)
	public ResponseEntity<?> handleUnderAge(UnderAgeException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "UNDER_AGE", "message", ex.getMessage()));
	}

	// 생년월일 파싱 실패
	@ExceptionHandler(InvalidBirthDateException.class)
	public ResponseEntity<?> handleInvalidBirthDate(InvalidBirthDateException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "INVALID_BIRTH_DATE", "message", ex.getMessage()));
	}

	// 개인정보 수집 미동의
	@ExceptionHandler(PrivacyAgreementRequiredException.class)
	public ResponseEntity<?> handlePrivacyAgreementRequired(PrivacyAgreementRequiredException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", "PRIVACY_AGREEMENT_REQUIRED", "message", ex.getMessage()));
	}

	// 리프레시 토큰 없음
	@ExceptionHandler(RefreshTokenNotFoundException.class)
	public ResponseEntity<?> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of("error", "REFRESH_TOKEN_NOT_FOUND", "message", ex.getMessage()));
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
