package kr.it.pullit.modules.questionset.client.exception;

import kr.it.pullit.shared.error.BusinessException;

public class LlmException extends BusinessException {

  private LlmException(Throwable cause, Object... args) {
    super(LlmErrorCode.LLM_GENERATION_FAILED, cause, args);
  }

  private LlmException(Object... args) {
    super(LlmErrorCode.LLM_GENERATION_FAILED, args);
  }

  public static LlmException generationFailed(String reason) {
    return new LlmException(reason);
  }

  public static LlmException withCause(Throwable cause) {
    return new LlmException(cause, cause.getMessage());
  }
}
