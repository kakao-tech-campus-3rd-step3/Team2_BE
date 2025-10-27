package kr.it.pullit.modules.auth.kakaoauth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import kr.it.pullit.support.annotation.MockitoUnitTest;

@MockitoUnitTest
@DisplayName("KakaoPrincipal 단위 테스트")
class KakaoPrincipalTest {

  @Test
  @DisplayName("OAuth2User 객체로부터 KakaoPrincipal을 성공적으로 생성한다")
  void shouldCreateKakaoPrincipalFromOAuth2User() {
    // given
    Map<String, Object> kakaoAccount =
        Map.of("email", "test@kakao.com", "profile", Map.of("nickname", "테스터"));
    Map<String, Object> attributes = Map.of("id", 12345L, "kakao_account", kakaoAccount);
    OAuth2User mockOAuth2User =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");

    // when
    KakaoPrincipal principal = KakaoPrincipal.from(mockOAuth2User);

    // then
    assertThat(principal.kakaoId()).isEqualTo(12345L);
    assertThat(principal.email()).isEqualTo("test@kakao.com");
    assertThat(principal.nickname()).isEqualTo("테스터");
  }

  @Test
  @DisplayName("이메일 정보가 없는 경우 가상 이메일을 생성한다")
  void shouldCreateVirtualEmailWhenEmailIsNull() {
    // given
    Map<String, Object> kakaoAccount = Map.of("profile", Map.of("nickname", "테스터"));
    Map<String, Object> attributes = Map.of("id", 12345L, "kakao_account", kakaoAccount);
    OAuth2User mockOAuth2User =
        new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");

    // when
    KakaoPrincipal principal = KakaoPrincipal.from(mockOAuth2User);

    // then
    assertThat(principal.kakaoId()).isEqualTo(12345L);
    assertThat(principal.email()).contains("@kakao.pullit");
    assertThat(principal.email().length()).isEqualTo(8 + "@kakao.pullit".length()); // 8자리 랜덤 ID
    assertThat(principal.nickname()).isEqualTo("테스터");
  }
}
