package com.wepong.pongdang.exception;

public class DonationNotFoundException extends RuntimeException {
	public DonationNotFoundException() {
		super(ExceptionMessage.DONATION_NOT_FOUND);
	}
}