package com.wepong.pongdang.exception;

public class NicknameAlreadyExist extends AuthException {
    public NicknameAlreadyExist() { super(ExceptionMessage.NICKNAME_ALREADY_EXIST); }
}

