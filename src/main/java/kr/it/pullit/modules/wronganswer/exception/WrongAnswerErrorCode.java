package kr.it.pullit.modules.wronganswer.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WrongAnswerErrorCode implements ErrorCode {
  WRONG_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "WAE_001", "오답을 찾을 수 없습니다. (%s: %s)"),
  NO_WRONG_ANSWERS_TO_REVIEW(HttpStatus.NOT_FOUND, "WAN_002", "복습할 오답이 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
