package com.wepong.pongdang.exception;

public class AlreadyTransferException extends RuntimeException {
    public AlreadyTransferException() {
        super(ExceptionMessage.ALREADY_TRANSFER_FINISHED);
    }
}
