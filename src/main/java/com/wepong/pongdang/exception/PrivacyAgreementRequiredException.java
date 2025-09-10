package com.wepong.pongdang.exception;

public class PrivacyAgreementRequiredException extends RuntimeException {
    public PrivacyAgreementRequiredException() {
        super(ExceptionMessage.PRIVACY_AGREEMENT_REQUIRED);
    }
}
