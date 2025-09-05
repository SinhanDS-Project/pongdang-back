package com.wepong.pongdang.exception;

public class UserCannotFoundException extends RuntimeException {
    public UserCannotFoundException() { super(ExceptionMessage.USER_CANNOT_FOUND); }
}

