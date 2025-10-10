package kr.it.pullit.modules.questionset.client.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LlmErrorCode implements ErrorCode {
  LLM_GENERATION_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR, "LLM_001", "LLM 콘텐츠 생성에 실패했습니다. (사유: %s)"),
  LLM_RESPONSE_PARSE_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR, "LLM_002", "LLM 응답을 파싱하는 중 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
