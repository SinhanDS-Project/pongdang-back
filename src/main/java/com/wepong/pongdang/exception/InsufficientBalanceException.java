package com.wepong.pongdang.exception;

public class InsufficientBalanceException extends RuntimeException {
	public InsufficientBalanceException() {
		super(ExceptionMessage.INSUFFICIENT_BALANCE);
	}
}