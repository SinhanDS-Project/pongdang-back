package com.wepong.pongdang.exception;

public class AlreadyAttendanceException extends RuntimeException {
    public AlreadyAttendanceException() {
        super(ExceptionMessage.ALREADY_ATTENDANCE_FINISHED);
    }
}
