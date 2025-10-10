package kr.it.pullit.shared.error;

import kr.it.pullit.platform.security.exception.InvalidRefreshTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(InvalidRefreshTokenException.class)
  protected ResponseEntity<ProblemDetail> handleInvalidRefreshTokenException(
      InvalidRefreshTokenException e) {
    log.error("handleInvalidRefreshTokenException", e);
    final ErrorCode errorCode = e.getErrorCode();

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(errorCode.getStatus(), errorCode.getMessage());
    problemDetail.setProperty("code", errorCode.getCode());

    return ResponseEntity.status(errorCode.getStatus()).body(problemDetail);
  }
}
