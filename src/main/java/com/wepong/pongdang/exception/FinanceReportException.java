package com.wepong.pongdang.exception;

public class FinanceReportException extends RuntimeException {
    public FinanceReportException() {
        super(ExceptionMessage.FINANCE_REPORT_NOT_GENERATED);
    }
}
