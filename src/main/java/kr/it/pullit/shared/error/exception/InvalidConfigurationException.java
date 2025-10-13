package kr.it.pullit.shared.error.exception;

import kr.it.pullit.shared.error.BusinessException;
import kr.it.pullit.shared.error.CommonErrorCode;

public class InvalidConfigurationException extends BusinessException {

  private InvalidConfigurationException(String message) {
    super(CommonErrorCode.INVALID_CONFIGURATION, message);
  }

  public static InvalidConfigurationException withMessage(String message) {
    return new InvalidConfigurationException(message);
  }
}
