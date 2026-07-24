package kr.it.pullit.platform.security.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.security.jwt.JwtProps;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.web.cookie.CookieManager;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

@MockitoUnitTest
@DisplayName("OAuth2AuthenticationSuccessHandler 단위 테스트")
class OAuth2AuthenticationSuccessHandlerTest {

  @InjectMocks private OAuth2AuthenticationSuccessHandler handler;

  @Mock private AuthService authService;
  @Mock private JwtProps jwtProps;
  @Mock private MemberPublicApi memberPublicApi;
  @Mock private CookieManager cookieManager;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private Authentication authentication;
  private OAuth2User oauth2User;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    authentication = mock(Authentication.class);
    oauth2User = mock(OAuth2User.class);

    given(authentication.getPrincipal()).willReturn(oauth2User);
    given(jwtProps.authorizedRedirectUris()).willReturn(List.of("https://frontend.pull.it.kr"));
    lenient().when(jwtProps.authorizedCookieDomains()).thenReturn(List.of(".pull.it.kr"));
  }

  @Nested
  @DisplayName("onAuthenticationSuccess")
  class DescribeOnAuthenticationSuccess {

    @Test
    @DisplayName("OAuth2 인증 성공 시 토큰을 발급하고 리다이렉트한다")
    void handlesAuthenticationSuccess() throws IOException {
      // given
      Long kakaoId = 123456L;
      final Long memberId = 1L;
      final var member = mock(Member.class);
      final var authTokens = new AuthTokens("access-token", "refresh-token");
      Map<String, Object> attributes = new HashMap<>();
      attributes.put("id", kakaoId);

      request.setServerName("frontend.pull.it.kr");

      given(oauth2User.getAttributes()).willReturn(attributes);
      given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
      given(member.getId()).willReturn(memberId);
      given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

      // when
      handler.onAuthenticationSuccess(request, response, authentication);

      // then
      verify(memberPublicApi).findByKakaoId(kakaoId);
      verify(authService).issueAndSaveTokens(memberId);
      verify(cookieManager)
          .addRefreshTokenCookie(eq(response), eq("refresh-token"), eq(".pull.it.kr"));
      assertThat(response.getRedirectedUrl()).contains("accessToken=access-token");
    }

    @Test
    @DisplayName("세션에 저장된 리다이렉션 URI를 사용한다")
    void usesRedirectUriFromSession() throws IOException {
      // given
      Long kakaoId = 123456L;
      final Long memberId = 1L;
      final var member = mock(Member.class);
      final var authTokens = new AuthTokens("access-token", "refresh-token");
      Map<String, Object> attributes = new HashMap<>();
      attributes.put("id", kakaoId);
      String customRedirectUri = "https://frontend.pull.it.kr/custom";

      request.setServerName("frontend.pull.it.kr");
      HttpSession session = request.getSession();
      session.setAttribute(
          OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY, customRedirectUri);

      given(oauth2User.getAttributes()).willReturn(attributes);
      given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
      given(member.getId()).willReturn(memberId);
      given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

      // when
      handler.onAuthenticationSuccess(request, response, authentication);

      // then
      assertThat(response.getRedirectedUrl()).contains(customRedirectUri);
      assertThat(session.getAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY))
          .isNull();
    }

    @Test
    @DisplayName("localhost인 경우 쿠키 도메인을 null로 설정한다")
    void setsCookieDomainToNullForLocalhost() throws IOException {
      // given
      Long kakaoId = 123456L;
      final Long memberId = 1L;
      final var member = mock(Member.class);
      final var authTokens = new AuthTokens("access-token", "refresh-token");
      Map<String, Object> attributes = new HashMap<>();
      attributes.put("id", kakaoId);

      request.setServerName("localhost");

      given(oauth2User.getAttributes()).willReturn(attributes);
      given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
      given(member.getId()).willReturn(memberId);
      given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

      // when
      handler.onAuthenticationSuccess(request, response, authentication);

      // then
      verify(cookieManager)
          .addRefreshTokenCookie(eq(response), eq("refresh-token"), eq((String) null));
    }
  }

  @Nested
  @DisplayName("determineTargetUrl")
  class DescribeDetermineTargetUrl {

    @Test
    @DisplayName("세션에 유효한 리다이렉션 URI가 있으면 해당 URI를 사용한다")
    void usesValidRedirectUriFromSession() {
      // given
      String validUri = "https://frontend.pull.it.kr/callback";
      HttpSession session = request.getSession();
      session.setAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY, validUri);

      // when
      String targetUrl = invokeDetermineTargetUrl("access-token");

      // then
      assertThat(targetUrl).contains(validUri);
      assertThat(targetUrl).contains("accessToken=access-token");
    }

    @Test
    @DisplayName("세션에 유효하지 않은 리다이렉션 URI가 있으면 기본 URI를 사용한다")
    void usesDefaultUriWhenInvalidRedirectUriInSession() {
      // given
      String invalidUri = "https://evil.com";
      HttpSession session = request.getSession();
      session.setAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY, invalidUri);

      // when
      String targetUrl = invokeDetermineTargetUrl("access-token");

      // then
      assertThat(targetUrl).contains("https://frontend.pull.it.kr");
      assertThat(targetUrl).contains("accessToken=access-token");
    }

    @Test
    @DisplayName("세션에 리다이렉션 URI가 없으면 기본 URI를 사용한다")
    void usesDefaultUriWhenNoRedirectUriInSession() {
      // when
      String targetUrl = invokeDetermineTargetUrl("access-token");

      // then
      assertThat(targetUrl).contains("https://frontend.pull.it.kr");
      assertThat(targetUrl).contains("accessToken=access-token");
    }

    private String invokeDetermineTargetUrl(String accessToken) {
      try {
        final var authTokens = new AuthTokens(accessToken, "refresh-token");
        Long kakaoId = 123456L;
        final Long memberId = 1L;
        final var member = mock(Member.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", kakaoId);

        request.setServerName("frontend.pull.it.kr");

        given(oauth2User.getAttributes()).willReturn(attributes);
        given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
        given(member.getId()).willReturn(memberId);
        given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

        handler.onAuthenticationSuccess(request, response, authentication);

        return response.getRedirectedUrl();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Nested
  @DisplayName("isAuthorizedRedirectUri")
  class DescribeIsAuthorizedRedirectUri {

    @Test
    @DisplayName("호스트와 포트가 일치하는 URI는 권한이 있다고 판단한다")
    void returnsTrueForMatchingHostAndPort() {
      // given
      given(jwtProps.authorizedRedirectUris())
          .willReturn(List.of("https://frontend.pull.it.kr:443"));

      // when
      boolean result = invokeIsAuthorizedRedirectUri("https://frontend.pull.it.kr:443/callback");

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("호스트가 다르면 권한이 없다고 판단한다")
    void returnsFalseForDifferentHost() {
      // given
      given(jwtProps.authorizedRedirectUris()).willReturn(List.of("https://frontend.pull.it.kr"));

      // when
      boolean result = invokeIsAuthorizedRedirectUri("https://evil.com");

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("포트가 다르면 권한이 없다고 판단한다")
    void returnsFalseForDifferentPort() {
      // given
      given(jwtProps.authorizedRedirectUris())
          .willReturn(List.of("https://frontend.pull.it.kr:443"));

      // when
      boolean result = invokeIsAuthorizedRedirectUri("https://frontend.pull.it.kr:8080");

      // then
      assertThat(result).isFalse();
    }

    private boolean invokeIsAuthorizedRedirectUri(String uri) {
      try {
        Long kakaoId = 123456L;
        final Long memberId = 1L;
        final var member = mock(Member.class);
        final var authTokens = new AuthTokens("access-token", "refresh-token");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", kakaoId);

        HttpSession session = request.getSession();
        session.setAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY, uri);
        request.setServerName("frontend.pull.it.kr");

        given(oauth2User.getAttributes()).willReturn(attributes);
        given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
        given(member.getId()).willReturn(memberId);
        given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

        handler.onAuthenticationSuccess(request, response, authentication);

        String redirectedUrl = response.getRedirectedUrl();
        return redirectedUrl != null && redirectedUrl.contains(uri);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Nested
  @DisplayName("determineBackendCookieDomain")
  class DescribeDetermineBackendCookieDomain {

    @Test
    @DisplayName("localhost인 경우 null을 반환한다")
    void returnsNullForLocalhost() throws IOException {
      // given
      Long kakaoId = 123456L;
      final Long memberId = 1L;
      final var member = mock(Member.class);
      final var authTokens = new AuthTokens("access-token", "refresh-token");
      Map<String, Object> attributes = new HashMap<>();
      attributes.put("id", kakaoId);

      request.setServerName("localhost");

      given(oauth2User.getAttributes()).willReturn(attributes);
      given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
      given(member.getId()).willReturn(memberId);
      given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

      // when
      handler.onAuthenticationSuccess(request, response, authentication);

      // then
      verify(cookieManager)
          .addRefreshTokenCookie(eq(response), eq("refresh-token"), eq((String) null));
    }

    @Test
    @DisplayName("설정된 하위 도메인 호스트는 설정된 쿠키 도메인을 반환한다")
    void returnsPullItKrDomainForMatchingHost() throws IOException {
      // given
      Long kakaoId = 123456L;
      final Long memberId = 1L;
      final var member = mock(Member.class);
      final var authTokens = new AuthTokens("access-token", "refresh-token");
      Map<String, Object> attributes = new HashMap<>();
      attributes.put("id", kakaoId);

      request.setServerName("frontend.pull.it.kr");

      given(oauth2User.getAttributes()).willReturn(attributes);
      given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
      given(member.getId()).willReturn(memberId);
      given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

      // when
      handler.onAuthenticationSuccess(request, response, authentication);

      // then
      verify(cookieManager)
          .addRefreshTokenCookie(eq(response), eq("refresh-token"), eq(".pull.it.kr"));
    }

    @Test
    @DisplayName("포트폴리오 호스트는 portfolio.yeon.world 쿠키 도메인을 반환한다")
    void returnsPortfolioDomainForMatchingHost() throws IOException {
      // given
      Long kakaoId = 123456L;
      final Long memberId = 1L;
      final var member = mock(Member.class);
      final var authTokens = new AuthTokens("access-token", "refresh-token");
      Map<String, Object> attributes = new HashMap<>();
      attributes.put("id", kakaoId);

      request.setServerName("portfolio.yeon.world");
      given(jwtProps.authorizedCookieDomains()).willReturn(List.of("portfolio.yeon.world"));
      given(oauth2User.getAttributes()).willReturn(attributes);
      given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
      given(member.getId()).willReturn(memberId);
      given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

      // when
      handler.onAuthenticationSuccess(request, response, authentication);

      // then
      verify(cookieManager)
          .addRefreshTokenCookie(eq(response), eq("refresh-token"), eq("portfolio.yeon.world"));
    }

    @Test
    @DisplayName("일치하는 도메인이 없으면 기본 도메인을 반환한다")
    void returnsDefaultDomainWhenNoMatch() throws IOException {
      // given
      Long kakaoId = 123456L;
      final Long memberId = 1L;
      final var member = mock(Member.class);
      final var authTokens = new AuthTokens("access-token", "refresh-token");
      Map<String, Object> attributes = new HashMap<>();
      attributes.put("id", kakaoId);

      request.setServerName("other-domain.com");
      given(jwtProps.authorizedCookieDomains()).willReturn(List.of(".pull.it.kr"));

      given(oauth2User.getAttributes()).willReturn(attributes);
      given(memberPublicApi.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
      given(member.getId()).willReturn(memberId);
      given(authService.issueAndSaveTokens(memberId)).willReturn(authTokens);

      // when
      handler.onAuthenticationSuccess(request, response, authentication);

      // then
      verify(cookieManager)
          .addRefreshTokenCookie(eq(response), eq("refresh-token"), eq(".pull.it.kr"));
    }
  }
}
