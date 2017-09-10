package org.bbz.stock.quanttrader.consts;

public class ErrorCodeException extends RuntimeException {

  private final int errorCode;

  public ErrorCodeException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode.toNum();

  }

  public ErrorCodeException(ErrorCode errorCode) {
    this.errorCode = errorCode.toNum();

  }

  public ErrorCodeException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public int getErrorCode() {
    return errorCode;
  }
}
