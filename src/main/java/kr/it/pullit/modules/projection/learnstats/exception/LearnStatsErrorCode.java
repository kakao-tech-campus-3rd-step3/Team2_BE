package kr.it.pullit.modules.projection.learnstats.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum LearnStatsErrorCode implements ErrorCode {
  INVALID_SOLVED_QUESTION_COUNT(HttpStatus.BAD_REQUEST, "LSP_001", "풀이한 문제 수는 0보다 커야 합니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
