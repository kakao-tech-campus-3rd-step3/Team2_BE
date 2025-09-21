package kr.it.pullit.modules.member.web.dto;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.MemberStatus;

public record MemberResponse(
    Long id, Long kakaoId, String email, String name, MemberStatus status) {

  public static MemberResponse from(Member member) {
    return new MemberResponse(
        member.getId(),
        member.getKakaoId(),
        member.getEmail(),
        member.getName(),
        member.getStatus());
  }
}
