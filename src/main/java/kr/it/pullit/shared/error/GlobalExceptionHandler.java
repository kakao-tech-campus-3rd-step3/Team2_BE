package kr.it.pullit.shared.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import kr.it.pullit.modules.auth.exception.InvalidRefreshTokenException;
import kr.it.pullit.modules.questionset.exception.QuestionSetFailedException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(InvalidRefreshTokenException.class)
  protected ResponseEntity<ProblemDetail> handleInvalidRefreshTokenException(
      InvalidRefreshTokenException e) {
    log.warn("InvalidRefreshTokenException occurred: {}", e.getMessage());
    ErrorCode authErrorCode = e.getErrorCode();
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(authErrorCode.getStatus(), e.getMessage());
    problemDetail.setProperty("code", authErrorCode.getCode());

    return ResponseEntity.status(authErrorCode.getStatus()).body(problemDetail);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException e) {
    log.warn("BusinessException occurred: {}", e.getMessage());
    ErrorCode errorCode = e.getErrorCode();
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(errorCode.getStatus(), e.getMessage());
    problemDetail.setProperty("code", errorCode.getCode());
    return ResponseEntity.status(errorCode.getStatus()).body(problemDetail);
  }

  @ExceptionHandler({QuestionSetNotReadyException.class, QuestionSetFailedException.class})
  public ResponseEntity<ProblemDetail> handleQuestionSetException(BusinessException e) {
    return createProblemDetailResponse(e.getErrorCode(), e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("handleIllegalArgumentException: {}", e.getMessage());
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    problemDetail.setProperty("code", "C_001");
    return problemDetail;
  }

  private ResponseEntity<ProblemDetail> createProblemDetailResponse(ErrorCode errorCode,
      String message) {
    log.warn("BusinessException: code={}, message={}", errorCode.getCode(), message);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), message);
    problemDetail.setProperty("code", errorCode.getCode());

    return ResponseEntity.status(errorCode.getStatus()).body(problemDetail);
  }
}
