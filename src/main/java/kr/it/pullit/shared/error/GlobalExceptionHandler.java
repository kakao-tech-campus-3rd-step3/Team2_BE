package kr.it.pullit.shared.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Arrays;
import java.util.stream.Collectors;
import kr.it.pullit.modules.auth.exception.InvalidRefreshTokenException;
import kr.it.pullit.modules.questionset.exception.QuestionSetFailedException;
import kr.it.pullit.modules.questionset.exception.QuestionSetNotReadyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    String message = createDetailedMessageForInvalidEnum(ex.getCause());

    log.warn("HttpMessageNotReadableException: {}", ex.getMessage());
    ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), message);
    problemDetail.setProperty("code", errorCode.getCode());
    return handleExceptionInternal(ex, problemDetail, headers, status, request);
  }

  private String createDetailedMessageForInvalidEnum(Throwable cause) {
    if (cause instanceof InvalidFormatException ife) {
      Class<?> targetType = ife.getTargetType();
      if (targetType != null && targetType.isEnum()) {
        String allowedValues =
            Arrays.stream(targetType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        return "정의되지 않은 enum 타입입니다. 지원되는 타입: " + allowedValues;
      }
    }
    return CommonErrorCode.INVALID_INPUT_VALUE.getMessage();
  }

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

  private ResponseEntity<ProblemDetail> createProblemDetailResponse(
      ErrorCode errorCode, String message) {
    log.warn("BusinessException: code={}, message={}", errorCode.getCode(), message);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), message);
    problemDetail.setProperty("code", errorCode.getCode());

    return ResponseEntity.status(errorCode.getStatus()).body(problemDetail);
  }
}
