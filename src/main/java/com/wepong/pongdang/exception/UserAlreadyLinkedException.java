package com.wepong.pongdang.exception;

public class UserAlreadyLinkedException extends RuntimeException {
    public UserAlreadyLinkedException() {
        super(ExceptionMessage.USER_ALREADY_LINKED);
    }
}
