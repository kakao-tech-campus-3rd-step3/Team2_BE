package kr.it.pullit.modules.member.exception;

import kr.it.pullit.shared.error.BusinessException;

public class MemberNotFoundException extends BusinessException {

  private MemberNotFoundException(Object... args) {
    super(MemberErrorCode.MEMBER_NOT_FOUND, args);
  }

  // 이름이 byId이므로 시그니처가 겹치지 않음
  public static MemberNotFoundException byId(long id) {
    return new MemberNotFoundException("ID", id);
  }

  // 이름이 byKakaoId이므로 시그니처가 겹치지 않음
  public static MemberNotFoundException byKakaoId(long kakaoId) {
    return new MemberNotFoundException("Kakao ID", kakaoId);
  }
}
