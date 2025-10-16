package kr.it.pullit.modules.auth.kakaoauth.domain;

import java.util.Map;
import java.util.UUID;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;
import org.springframework.security.oauth2.core.user.OAuth2User;

public record KakaoPrincipal(Long kakaoId, String email, String nickname) {

  @SuppressWarnings("unchecked")
  public static KakaoPrincipal from(OAuth2User oauth2User) {
    Map<String, Object> attributes = oauth2User.getAttributes();
    Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

    Long kakaoId = (Long) attributes.get("id");
    String email = (String) kakaoAccount.get("email");
    String nickname = (String) profile.get("nickname");

    if (email == null) {
      String randomId = UUID.randomUUID().toString().substring(0, 8);
      email = randomId + "@kakao.pullit";
    }

    return new KakaoPrincipal(kakaoId, email, nickname);
  }

  public SocialLoginCommand toSocialLoginCommand() {
    return SocialLoginCommand.kakao(this.kakaoId, this.email, this.nickname);
  }
}
