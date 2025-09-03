package com.wepong.pongdang.exception;

public class ReplyUnauthorizedException extends RuntimeException {
    public ReplyUnauthorizedException() {
        super(ExceptionMessage.REPLY_UNAUTHORIZED);
    }
}