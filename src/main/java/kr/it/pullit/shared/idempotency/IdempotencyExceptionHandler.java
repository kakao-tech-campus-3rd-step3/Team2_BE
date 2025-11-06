package kr.it.pullit.shared.idempotency;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class IdempotencyExceptionHandler {

  @ExceptionHandler(DuplicateRequestException.class)
  public ResponseEntity<Map<String, Object>> handleDup(DuplicateRequestException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("error", "duplicate_request", "message", ex.getMessage()));
  }
}
