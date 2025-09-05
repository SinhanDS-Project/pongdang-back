package com.wepong.pongdang.exception;

public class ProductNotFoundException extends RuntimeException {
	public ProductNotFoundException() {
		super(ExceptionMessage.PRODUCT_NOT_FOUND);
	}
}