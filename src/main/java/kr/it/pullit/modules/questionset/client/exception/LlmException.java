package kr.it.pullit.modules.questionset.client.exception;

import kr.it.pullit.shared.error.BusinessException;
import lombok.Getter;

@Getter
public class LlmException extends BusinessException {

  private final LlmErrorType errorType;

  private LlmException(LlmErrorType errorType, Throwable cause, Object... args) {
    super(LlmErrorCode.LLM_GENERATION_FAILED, cause, args);
    this.errorType = errorType;
  }

  private LlmException(LlmErrorType errorType, Object... args) {
    super(LlmErrorCode.LLM_GENERATION_FAILED, args);
    this.errorType = errorType;
  }

  public static LlmException ofTemporary(String reason) {
    return new LlmException(LlmErrorType.TEMPORARY, reason);
  }

  public static LlmException ofTemporary(Throwable cause) {
    return new LlmException(LlmErrorType.TEMPORARY, cause, cause.getMessage());
  }

  public static LlmException from(Throwable cause) {
    return ofTemporary(cause);
  }

  public static LlmException ofPermanent(String reason) {
    return new LlmException(LlmErrorType.PERMANENT, reason);
  }

  public static LlmException permanent(String reason) {
    return new LlmException(LlmErrorType.PERMANENT, reason);
  }

  public static LlmException permanent(Throwable cause) {
    return new LlmException(LlmErrorType.PERMANENT, cause, cause.getMessage());
  }
}
