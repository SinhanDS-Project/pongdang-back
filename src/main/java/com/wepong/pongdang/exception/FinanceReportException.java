package com.wepong.pongdang.exception;

public class FinanceReportException extends RuntimeException {
    public FinanceReportException() {
        super(ExceptionMessage.FINANCE_REPORT_GENERATE_ERROR);
    }
}
