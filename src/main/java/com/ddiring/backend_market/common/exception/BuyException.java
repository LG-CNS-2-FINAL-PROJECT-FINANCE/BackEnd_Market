package com.ddiring.backend_market.common.exception;

public class BuyException extends ClientError{
    public BuyException(String message) {
        this.errorCode = "잔고가 부족합니다.";
        this.errorMessage = message;
    }
}
