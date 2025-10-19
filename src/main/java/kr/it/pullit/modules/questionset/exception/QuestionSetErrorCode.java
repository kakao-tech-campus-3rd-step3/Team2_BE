package kr.it.pullit.modules.questionset.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum QuestionSetErrorCode implements ErrorCode {
  QUESTION_SET_NOT_FOUND(HttpStatus.NOT_FOUND, "QSE_001", "문제집을 찾을 수 없습니다. (%s: %s)"),
  QUESTION_SET_NOT_READY(HttpStatus.BAD_REQUEST, "QSE_002", "문제집이 아직 생성 중입니다."),
  QUESTION_SET_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "QSE_003", "문제집 생성에 실패했습니다."),
  WRONG_ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "QSE_004", "복습할 오답이 없습니다."),
  QUESTION_SET_NOT_COMPLETE(HttpStatus.BAD_REQUEST, "QSE_005", "문제집이 완료되지 않았습니다."),
  QUESTION_SET_UNAUTHORIZED(HttpStatus.FORBIDDEN, "QSE_006", "해당 문제집에 대한 권한이 없습니다."),
  SOURCE_NOT_READY(HttpStatus.BAD_REQUEST, "QSE_007", "아직 처리 중인 소스 파일이라 문제집을 생성할 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
