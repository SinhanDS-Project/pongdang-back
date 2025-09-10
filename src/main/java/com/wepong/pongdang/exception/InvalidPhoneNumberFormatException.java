package com.wepong.pongdang.exception;

public class InvalidPhoneNumberFormatException extends RuntimeException {
    public InvalidPhoneNumberFormatException() {
        super(ExceptionMessage.INVALID_PHONE_NUMBER_FORMAT);
    }
}
