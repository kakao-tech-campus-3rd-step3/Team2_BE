package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum QuestionErrorCode implements ErrorCode {
  QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QE_001", "문제를 찾을 수 없습니다. (%s: %s)");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
