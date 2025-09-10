package com.wepong.pongdang.exception;

public class InvalidBirthDateException extends RuntimeException {
    public InvalidBirthDateException() {
        super(ExceptionMessage.INVALID_BIRTH_DATE);
    }
}
