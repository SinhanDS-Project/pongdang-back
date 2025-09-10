package com.wepong.pongdang.exception;

public class MissingRequiredFieldsException extends RuntimeException {
    public MissingRequiredFieldsException() {
        super(ExceptionMessage.MISSING_REQUIRED_FIELDS);
    }
}