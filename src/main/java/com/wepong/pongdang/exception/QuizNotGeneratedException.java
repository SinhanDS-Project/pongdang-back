package com.wepong.pongdang.exception;

public class QuizNotGeneratedException extends RuntimeException {
    public QuizNotGeneratedException() { super(ExceptionMessage.QUIZ_NOT_GENERATED); }
}
