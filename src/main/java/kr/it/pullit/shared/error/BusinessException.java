package kr.it.pullit.shared.error;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BusinessException(ErrorCode errorCode, Object... args) {
    super(String.format(errorCode.getMessage(), args));
    this.errorCode = errorCode;
  }
}
