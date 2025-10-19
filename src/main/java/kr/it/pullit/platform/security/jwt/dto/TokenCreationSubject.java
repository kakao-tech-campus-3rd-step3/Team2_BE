package kr.it.pullit.platform.security.jwt.dto;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;

public record TokenCreationSubject(Long memberId, String email, Role role) {

  public static TokenCreationSubject from(Member member) {
    return new TokenCreationSubject(member.getId(), member.getEmail(), Role.MEMBER);
  }
}
