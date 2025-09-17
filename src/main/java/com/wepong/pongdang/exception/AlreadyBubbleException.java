package com.wepong.pongdang.exception;

public class AlreadyBubbleException extends RuntimeException {
    public AlreadyBubbleException() {
        super(ExceptionMessage.ALREADY_BUBBLE_FINISHED);
    }
}
