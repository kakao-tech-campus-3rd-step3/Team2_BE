package kr.it.pullit.modules.member.exception;

import kr.it.pullit.shared.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements ErrorCode {
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEM_001", "%s '%s'에 해당하는 회원을 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
