package com.wepong.pongdang.exception;

public class BettingUserNotFoundException extends RuntimeException {
    public BettingUserNotFoundException() {
        super(ExceptionMessage.BETTINGUSER_NOT_FOUND);
    }
}
