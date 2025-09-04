package com.wepong.pongdang.exception;

public class QuizAlreadyTakenException extends RuntimeException {
    public QuizAlreadyTakenException() { super(ExceptionMessage.ALREADY_TODAY_QUIZ_FINISHED); }
}
