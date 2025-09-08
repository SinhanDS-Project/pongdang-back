package com.wepong.pongdang.exception;

public class RoomNotFoundException extends RuntimeException {
	public RoomNotFoundException() {
		super(ExceptionMessage.ROOM_NOT_FOUND);
	}
}