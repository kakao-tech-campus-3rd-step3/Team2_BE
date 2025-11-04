package kr.it.pullit.modules.auth.kakaoauth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import kr.it.pullit.modules.auth.kakaoauth.domain.KakaoPrincipal;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.member.service.dto.SocialLoginCommand;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.fixture.MemberFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

@MockitoUnitTest
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("CustomOAuth2UserService 단위 테스트")
class CustomOAuth2UserServiceTest {

  @Mock private MemberPublicApi memberPublicApi;

  private OAuth2User createMockOAuth2User() {
    Map<String, Object> kakaoAccount =
        Map.of("email", "test@kakao.com", "profile", Map.of("nickname", "테스터"));
    Map<String, Object> attributes = Map.of("id", 12345L, "kakao_account", kakaoAccount);
    return new DefaultOAuth2User(
        Collections.singleton(new SimpleGrantedAuthority("ROLE_MEMBER")), attributes, "id");
  }

  private CustomOAuth2UserService spyService() {
    return spy(new CustomOAuth2UserService(memberPublicApi));
  }

  @Nested
  @DisplayName("loadUser")
  class DescribeLoadUser {

    @Test
    @DisplayName("멤버를 찾거나 생성하여 OAuth2User를 반환한다")
    void returnsOAuth2UserWhenMemberFoundOrCreated() throws OAuth2AuthenticationException {
      // given
      CustomOAuth2UserService service = spyService();
      OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
      OAuth2User oauth2User = createMockOAuth2User();
      Member member = MemberFixtures.basicUser();

      doReturn(oauth2User).when(service).fetchOAuth2User(any(OAuth2UserRequest.class));
      given(memberPublicApi.findOrCreateMember(any(SocialLoginCommand.class)))
          .willReturn(Optional.of(member));

      // when
      OAuth2User result = service.loadUser(userRequest);

      // then
      assertThat(result).isInstanceOf(DefaultOAuth2User.class);
      DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) result;
      assertThat(defaultOAuth2User.getAuthorities()).hasSize(1);
      assertThat(defaultOAuth2User.getAuthorities().iterator().next().getAuthority())
          .isEqualTo(Role.MEMBER.getKey());
      assertThat(defaultOAuth2User.getAttributes()).isEqualTo(oauth2User.getAttributes());
    }

    @Test
    @DisplayName("멤버를 찾지 못하면 OAuth2AuthenticationException을 발생시킨다")
    void throwsExceptionWhenMemberNotFound() throws OAuth2AuthenticationException {
      // given
      CustomOAuth2UserService service = spyService();
      OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
      OAuth2User oauth2User = createMockOAuth2User();
      KakaoPrincipal kakaoPrincipal = KakaoPrincipal.from(oauth2User);

      doReturn(oauth2User).when(service).fetchOAuth2User(any(OAuth2UserRequest.class));
      given(memberPublicApi.findOrCreateMember(any(SocialLoginCommand.class)))
          .willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> service.loadUser(userRequest))
          .isInstanceOf(OAuth2AuthenticationException.class)
          .hasCauseInstanceOf(MemberNotFoundException.class)
          .hasMessageContaining(kakaoPrincipal.kakaoId().toString());
    }

    @Test
    @DisplayName("ADMIN 역할을 가진 멤버의 경우 ADMIN 권한을 가진 OAuth2User를 반환한다")
    void returnsOAuth2UserWithAdminAuthorityWhenMemberIsAdmin()
        throws OAuth2AuthenticationException {
      // given
      CustomOAuth2UserService service = spyService();
      OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
      OAuth2User oauth2User = createMockOAuth2User();
      Member adminMember = MemberFixtures.basicAdmin();

      doReturn(oauth2User).when(service).fetchOAuth2User(any(OAuth2UserRequest.class));
      given(memberPublicApi.findOrCreateMember(any(SocialLoginCommand.class)))
          .willReturn(Optional.of(adminMember));

      // when
      OAuth2User result = service.loadUser(userRequest);

      // then
      assertThat(result).isInstanceOf(DefaultOAuth2User.class);
      DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) result;
      assertThat(defaultOAuth2User.getAuthorities()).hasSize(1);
      assertThat(defaultOAuth2User.getAuthorities().iterator().next().getAuthority())
          .isEqualTo(Role.ADMIN.getKey());
    }
  }
}
