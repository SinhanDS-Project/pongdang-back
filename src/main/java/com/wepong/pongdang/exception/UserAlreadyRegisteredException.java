package com.wepong.pongdang.exception;

public class UserAlreadyRegisteredException extends RuntimeException {
    public UserAlreadyRegisteredException() {
        super(ExceptionMessage.USER_ALREADY_REGISTERED);
    }
}
