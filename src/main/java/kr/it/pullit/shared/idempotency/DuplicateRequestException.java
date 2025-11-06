package kr.it.pullit.shared.idempotency;

import kr.it.pullit.shared.error.BusinessException;
import kr.it.pullit.shared.error.CommonErrorCode;

public class DuplicateRequestException extends BusinessException {

  public DuplicateRequestException(String message) {
    super(CommonErrorCode.DUPLICATE_REQUEST, message);
  }

  public DuplicateRequestException() {
    super(CommonErrorCode.DUPLICATE_REQUEST);
  }
}
