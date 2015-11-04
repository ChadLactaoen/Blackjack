package com.lactaoen.blackjack.exception;

public class BlackjackException extends Exception {

    private BlackjackErrorCode errorCode;

    public BlackjackException(BlackjackErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BlackjackErrorCode getErrorCode() {
        return errorCode;
    }
}
