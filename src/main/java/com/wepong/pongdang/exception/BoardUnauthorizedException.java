package com.wepong.pongdang.exception;

public class BoardUnauthorizedException extends RuntimeException {
    public BoardUnauthorizedException() {
        super(ExceptionMessage.BOARD_UNAUTHORIZED);
    }
}