package com.wepong.pongdang.exception;

public class ReplyNotFoundException extends RuntimeException {
    public ReplyNotFoundException() {
        super(ExceptionMessage.REPLY_NOT_FOUND);
    }
}