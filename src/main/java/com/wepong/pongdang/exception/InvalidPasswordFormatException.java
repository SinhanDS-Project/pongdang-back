package com.wepong.pongdang.exception;

public class InvalidPasswordFormatException extends RuntimeException {
    public InvalidPasswordFormatException() {
        super(ExceptionMessage.INVALID_PASSWORD_FORMAT);
    }
}
