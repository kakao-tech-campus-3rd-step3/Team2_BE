package kr.it.pullit.modules.member.service.dto;

import lombok.Builder;

@Builder
public record SocialLoginCommand(Long kakaoId, String email, String name, Provider provider) {

  public enum Provider {
    KAKAO
  }

  public static SocialLoginCommand kakao(Long kakaoId, String email, String name) {
    return SocialLoginCommand.builder()
        .kakaoId(kakaoId)
        .email(email)
        .name(name)
        .provider(Provider.KAKAO)
        .build();
  }
}
