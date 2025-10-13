package kr.it.pullit.shared.error;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
  HttpStatus getStatus();

  String getCode();

  String getMessage();
}
