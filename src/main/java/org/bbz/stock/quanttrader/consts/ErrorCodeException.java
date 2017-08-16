package org.bbz.stock.quanttrader.consts;

public class ErrorCodeException extends RuntimeException{
    private final ErrorCode errorCode;

    public ErrorCodeException( ErrorCode errorCode, String message ){
        super( message );
        this.errorCode = errorCode;

    }

    public ErrorCodeException( ErrorCode errorCode ){
        this.errorCode = errorCode;

    }

    public ErrorCode getErrorCode(){
        return errorCode;
    }
}
