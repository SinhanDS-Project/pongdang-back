package com.wepong.pongdang.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException() {
        super(ExceptionMessage.REFRESH_TOKEN_NOT_FOUND);
    }
}
