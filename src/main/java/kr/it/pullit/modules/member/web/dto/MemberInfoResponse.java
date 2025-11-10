package kr.it.pullit.modules.member.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.it.pullit.modules.member.domain.entity.Member;
import lombok.Builder;

@Builder
public record MemberInfoResponse(
    @Schema(description = "회원 ID", example = "1") Long id,
    @Schema(description = "회원 이름", example = "홍길동") String name,
    @Schema(description = "회원 이메일", example = "gildong@example.com") String email) {

  public static MemberInfoResponse from(Member member) {
    return MemberInfoResponse.builder()
        .id(member.getId())
        .name(member.getName())
        .email(member.getEmail())
        .build();
  }
}
