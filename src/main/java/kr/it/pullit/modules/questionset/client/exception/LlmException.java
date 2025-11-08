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

  /**
   * 다른 예외(cause)를 감싸서 일시적인 LlmException으로 변환합니다.
   *
   * @param cause 근본 원인이 되는 예외
   * @return 원인을 포함하는 새로운 LlmException
   */
  public static LlmException from(Throwable cause) {
    return ofTemporary(cause);
  }

  public static LlmException ofPermanent(String reason) {
    return new LlmException(LlmErrorType.PERMANENT, reason);
  }
}
