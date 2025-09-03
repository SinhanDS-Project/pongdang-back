package com.wepong.pongdang.exception;

public class BoardNotFoundException extends RuntimeException {
    public BoardNotFoundException() {
        super(ExceptionMessage.BOARD_NOT_FOUND);
    }
}