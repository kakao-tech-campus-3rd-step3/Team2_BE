package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestionErrorCode implements ErrorCode {
  QUESTION_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "Q_001", "문제 유형은 필수입니다."),
  MULTIPLE_CHOICE_OPTIONS_REQUIRED(HttpStatus.BAD_REQUEST, "Q_002", "객관식 문제는 선지가 필수입니다."),
  TRUE_FALSE_NO_OPTIONS(HttpStatus.BAD_REQUEST, "Q_003", "OX 문제는 선지가 없어야 합니다."),
  TRUE_FALSE_INVALID_ANSWER(HttpStatus.BAD_REQUEST, "Q_004", "OX 문제의 정답은 '참' 또는 '거짓'이어야 합니다."),
  SHORT_ANSWER_NO_OPTIONS(HttpStatus.BAD_REQUEST, "Q_005", "단답형 문제는 선지가 없어야 합니다."),
  QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Q_006", "문제를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
