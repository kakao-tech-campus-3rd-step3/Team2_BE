package kr.it.pullit.modules.questionset.client.exception;

import kr.it.pullit.shared.error.BusinessException;

public class LlmResponseParseException extends BusinessException {

  private LlmResponseParseException(Throwable cause) {
    super(LlmErrorCode.LLM_RESPONSE_PARSE_FAILED, cause);
  }

  public static LlmResponseParseException create(Throwable cause) {
    return new LlmResponseParseException(cause);
  }
}
