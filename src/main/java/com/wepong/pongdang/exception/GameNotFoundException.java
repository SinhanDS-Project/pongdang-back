package com.wepong.pongdang.exception;

public class GameNotFoundException extends RuntimeException {
	public GameNotFoundException() {
		super(ExceptionMessage.GAME_NOT_FOUND);
	}
}