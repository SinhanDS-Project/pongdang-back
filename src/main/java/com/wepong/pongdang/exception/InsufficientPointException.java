package com.wepong.pongdang.exception;

public class InsufficientPointException extends RuntimeException {
    public InsufficientPointException() {
        super(ExceptionMessage.INSUFFICIENT_POINT);
    }
}
