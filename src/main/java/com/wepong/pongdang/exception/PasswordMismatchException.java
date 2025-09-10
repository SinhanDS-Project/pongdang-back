package com.wepong.pongdang.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException() {
        super(ExceptionMessage.PASSWORD_MISMATCH);
    }
}
