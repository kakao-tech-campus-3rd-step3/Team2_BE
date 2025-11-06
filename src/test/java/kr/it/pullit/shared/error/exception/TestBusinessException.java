package kr.it.pullit.shared.error.exception;

import kr.it.pullit.shared.error.BusinessException;

public class TestBusinessException extends BusinessException {
  public TestBusinessException() {
    super(TestErrorCode.TEST_ERROR);
  }
}
