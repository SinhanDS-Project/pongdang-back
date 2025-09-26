package com.wepong.pongdang.exception;

public class ExceptionMessage {
	// User
    public static final String USER_CANNOT_FOUND = "회원 정보를 찾을 수 없습니다.";
    public static final String USER_NOT_FOUND = "입력한 이메일이 존재하지 않습니다.";
    public static final String EMAIL_NOT_FOUND = "가입된 이메일이 존재하지 않습니다.";
    public static final String INVALID_PASSWORD = "입력하신 아이디 또는 비밀번호가 다릅니다.";
    public static final String MISSING_CREDENTIALS = "아이디와 비밀번호 모두 입력 후 가능합니다.";
    public static final String SESSION_EXPIRED = "다시 로그인이 필요합니다.";
    public static final String UNAUTHORIZED_ACCESS = "로그인 후 이용이 가능한 서비스입니다.";
    public static final String INVALID_TOKEN = "잘못된 토큰입니다.";
    public static final String INVALID_UPDATE_PASSWORD = "현재 비밀번호가 올바르지 않습니다.";
    public static final String REFRESH_TOKEN_NOT_FOUND = "리프레시 토큰이 없습니다.";
    public static final String USER_ALREADY_REGISTERED = "이미 가입된 사용자입니다.";

    // Validation
    public static final String MISSING_REQUIRED_FIELDS = "모든 필수 항목을 입력해주세요.";
    public static final String EMAIL_ALREADY_EXISTS = "이미 사용 중인 이메일입니다.";
    public static final String NICKNAME_ALREADY_EXIST = "이미 사용 중인 닉네임입니다.";
    public static final String PASSWORD_MISMATCH = "비밀번호가 일치하지 않습니다.";
    public static final String INVALID_PASSWORD_FORMAT = "비밀번호는 6~8자의 대소문자, 숫자, 특수문자를 포함해야 합니다.";
    public static final String INVALID_PHONE_NUMBER_FORMAT = "전화번호 형식이 올바르지 않습니다. (예: 010-0000-0000)";
    public static final String UNDER_AGE = "만 15세 이상만 가입할 수 있습니다.";
    public static final String INVALID_BIRTH_DATE = "유효한 생년월일을 입력해주세요.";
    public static final String PRIVACY_AGREEMENT_REQUIRED = "개인정보 수집 및 이용에 동의해야 가입할 수 있습니다.";

	// Board
    public static final String BOARD_NOT_FOUND = "게시글이 존재하지 않습니다.";
    public static final String BOARD_UNAUTHORIZED = "본인 게시글만 수정/삭제할 수 있습니다.";
    public static final String REPLY_NOT_FOUND = "댓글이 존재하지 않습니다.";
    public static final String REPLY_UNAUTHORIZED = "본인 댓글만 수정/삭제할 수 있습니다.";

	// AI
	public static final String FINANCE_REPORT_NOT_GENERATED = "금융 리포트 생성에 실패하였습니다. 잠시 후 다시 이용해 주세요.";
    public static final String QUIZ_NOT_GENERATED = "오늘의 퀴즈가 아직 생성되지 않았습니다. 잠시 후 다시 이용해 주세요.";
    public static final String ALREADY_TODAY_QUIZ_FINISHED = "오늘 퀴즈는 이미 참여 완료되었습니다.";

	public static final String ALREADY_ATTENDANCE_FINISHED = "이미 오늘의 출석이 완료되었습니다.";
	public static final String ALREADY_BUBBLE_FINISHED = "이미 오늘의 물방울 터트리기가 완료되었습니다.";
	public static final String ALREADY_TRANSFER_FINISHED = "이미 오늘의 환율 맞추기가 완료되었습니다.";

	// Product
	public static final String PRODUCT_NOT_FOUND = "상품이 존재하지 않습니다.";

	// Donation
	public static final String DONATION_NOT_FOUND = "기부 정보가 존재하지 않습니다.";

    //BettingUser
    public static final String INSUFFICIENT_POINT = "사용자의 신한 마이 포인트가 부족합니다";
    public static final String BETTINGUSER_NOT_FOUND = "신한 회원이 아닙니다 ";
    public static final String USER_ALREADY_LINKED = "이미 연동된 사용자입니다.";

    // Game
    public static final String GAME_NOT_FOUND = "게임이 존재하지 않습니다.";
    public static final String ROOM_NOT_FOUND = "게임방이 존재하지 않습니다.";

    // Wallet
    public static final String INSUFFICIENT_BALANCE = "퐁 잔액이 부족합니다.";
}
