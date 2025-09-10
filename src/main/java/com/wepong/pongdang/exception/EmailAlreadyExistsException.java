package com.wepong.pongdang.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super(ExceptionMessage.EMAIL_ALREADY_EXISTS);
    }
}
