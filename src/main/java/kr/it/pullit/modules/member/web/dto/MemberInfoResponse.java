package kr.it.pullit.modules.member.web.dto;

import kr.it.pullit.modules.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record MemberInfoResponse(Long id, String email, String name) {

  public static MemberInfoResponse from(Member member) {
    return MemberInfoResponse.builder()
        .id(member.getId())
        .email(member.getEmail())
        .name(member.getName())
        .build();
  }
}
