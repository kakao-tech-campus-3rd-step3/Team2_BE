package kr.it.pullit.shared.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A006", "리프레시 토큰이 유효하지 않습니다."),

  // Member
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
