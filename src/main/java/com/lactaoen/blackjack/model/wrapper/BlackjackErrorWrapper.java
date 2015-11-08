package com.lactaoen.blackjack.model.wrapper;

import com.lactaoen.blackjack.exception.BlackjackErrorCode;

public class BlackjackErrorWrapper {

    private BlackjackErrorCode errorCode;
    private String message;

    public BlackjackErrorWrapper() {
    }

    public BlackjackErrorWrapper(BlackjackErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public BlackjackErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(BlackjackErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
