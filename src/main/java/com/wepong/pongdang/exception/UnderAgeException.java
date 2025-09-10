package com.wepong.pongdang.exception;

public class UnderAgeException extends RuntimeException {
    public UnderAgeException() {
        super(ExceptionMessage.UNDER_AGE);
    }
}
