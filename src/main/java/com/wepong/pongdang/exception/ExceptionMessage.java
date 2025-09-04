package com.wepong.pongdang.exception;

public class ExceptionMessage {
	public static final String USER_NOT_FOUND = "입력한 이메일이 존재하지 않습니다";
	public static final String EMAIL_NOT_FOUND = "가입된 이메일이 존재하지 않습니다.";
	public static final String INVALID_PASSWORD = "입력하신 아이디 또는 비밀번호가 다릅니다.";
	public static final String MISSING_CREDENTIALS = "아이디와 비밀번호 모두 입력 후 가능합니다.";
	public static final String SESSION_EXPIRED = "다시 로그인이 필요합니다.";
	public static final String UNAUTHORIZED_ACCESS = "로그인 후 이용이 가능한 서비스입니다.";
	public static final String INVALID_TOKEN = "잘못된 토큰입니다.";
	public static final String INVALID_UPDATE_PASSWORD = "현재 비밀번호가 올바르지 않습니다.";
	public static final String ALREADY_TODAY_QUIZ_FINISHED = "오늘 퀴즈는 이미 참여 완료되었습니다.";
    public static final String BOARD_NOT_FOUND = "게시글이 존재하지 않습니다.";
    public static final String BOARD_UNAUTHORIZED = "본인 게시글만 수정/삭제할 수 있습니다.";
    public static final String REPLY_NOT_FOUND = "댓글이 존재하지 않습니다.";
    public static final String REPLY_UNAUTHORIZED = "본인 댓글만 수정/삭제할 수 있습니다.";

}
