package kr.it.pullit.platform.web.cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.util.List;
import kr.it.pullit.platform.security.jwt.JwtProps;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@MockitoUnitTest
@DisplayName("CookieManager 단위 테스트")
class CookieManagerTest {

  @InjectMocks private CookieManager cookieManager;

  @Mock private JwtProps jwtProps;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Nested
  @DisplayName("addRefreshTokenCookie")
  class DescribeAddRefreshTokenCookie {

    @Test
    @DisplayName("리프레시 토큰 쿠키를 추가한다")
    void addsRefreshTokenCookie() {
      // given
      String refreshToken = "test-refresh-token";
      String domain = "portfolio.yeon.world";
      given(jwtProps.refreshTokenExpirationDays()).willReturn(Duration.ofDays(7));
      given(jwtProps.refreshTokenCookiePath()).willReturn("/pull-it/auth/refresh");

      // when
      cookieManager.addRefreshTokenCookie(response, refreshToken, domain);

      // then
      String setCookieHeader = response.getHeader("Set-Cookie");
      assertThat(setCookieHeader).contains("refresh_token=test-refresh-token");
      assertThat(setCookieHeader).contains("Domain=portfolio.yeon.world");
      assertThat(setCookieHeader).contains("Path=/pull-it/auth/refresh");
      assertThat(setCookieHeader).contains("HttpOnly");
      assertThat(setCookieHeader).contains("Secure");
      assertThat(setCookieHeader).contains("SameSite=None");
    }

    @Test
    @DisplayName("도메인이 null이면 도메인 속성을 설정하지 않는다")
    void doesNotSetDomainWhenDomainIsNull() {
      // given
      String refreshToken = "test-refresh-token";
      given(jwtProps.refreshTokenExpirationDays()).willReturn(Duration.ofDays(7));

      // when
      cookieManager.addRefreshTokenCookie(response, refreshToken, null);

      // then
      String setCookieHeader = response.getHeader("Set-Cookie");
      assertThat(setCookieHeader).contains("refresh_token=test-refresh-token");
      assertThat(setCookieHeader).doesNotContain("Domain=");
    }

    @Test
    @DisplayName("도메인이 빈 문자열이면 도메인 속성을 설정하지 않는다")
    void doesNotSetDomainWhenDomainIsBlank() {
      // given
      String refreshToken = "test-refresh-token";
      given(jwtProps.refreshTokenExpirationDays()).willReturn(Duration.ofDays(7));

      // when
      cookieManager.addRefreshTokenCookie(response, refreshToken, "");

      // then
      String setCookieHeader = response.getHeader("Set-Cookie");
      assertThat(setCookieHeader).contains("refresh_token=test-refresh-token");
      assertThat(setCookieHeader).doesNotContain("Domain=");
    }
  }

  @Nested
  @DisplayName("expireCookie")
  class DescribeExpireCookie {

    @Test
    @DisplayName("쿠키를 만료시킨다")
    void expiresCookie() {
      // given
      String cookieName = "test-cookie";
      request.setServerName("portfolio.yeon.world");
      given(jwtProps.authorizedCookieDomains()).willReturn(List.of("portfolio.yeon.world"));

      // when
      cookieManager.expireCookie(request, response, cookieName);

      // then
      String setCookieHeader = response.getHeader("Set-Cookie");
      assertThat(setCookieHeader).contains("test-cookie=");
      assertThat(setCookieHeader).contains("Max-Age=0");
      assertThat(setCookieHeader).contains("Domain=portfolio.yeon.world");
      assertThat(setCookieHeader).contains("Path=/");
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키를 만료시킬 때 올바른 경로를 사용한다")
    void expiresRefreshTokenCookieWithCorrectPath() {
      // given
      request.setServerName("portfolio.yeon.world");
      given(jwtProps.authorizedCookieDomains()).willReturn(List.of("portfolio.yeon.world"));
      given(jwtProps.refreshTokenCookiePath()).willReturn("/pull-it/auth/refresh");
      String cookieName = "refresh_token";

      // when
      cookieManager.expireCookie(request, response, cookieName);

      // then
      String setCookieHeader = response.getHeader("Set-Cookie");
      assertThat(setCookieHeader).contains("refresh_token=");
      assertThat(setCookieHeader).contains("Max-Age=0");
      assertThat(setCookieHeader).contains("Domain=portfolio.yeon.world");
      assertThat(setCookieHeader).contains("Path=/pull-it/auth/refresh");
    }

    @Test
    @DisplayName("호스트가 null이면 기본 도메인을 사용한다")
    void usesDefaultDomainWhenHostIsNull() {
      // given
      String cookieName = "test-cookie";
      request.setServerName(null);
      given(jwtProps.authorizedCookieDomains()).willReturn(List.of("portfolio.yeon.world"));

      // when
      cookieManager.expireCookie(request, response, cookieName);

      // then
      String setCookieHeader = response.getHeader("Set-Cookie");
      assertThat(setCookieHeader).contains("Domain=portfolio.yeon.world");
    }

    @Test
    @DisplayName("일치하는 도메인이 없으면 기본 도메인을 사용한다")
    void usesDefaultDomainWhenNoMatch() {
      // given
      String cookieName = "test-cookie";
      request.setServerName("other-domain.com");
      given(jwtProps.authorizedCookieDomains()).willReturn(List.of("portfolio.yeon.world"));

      // when
      cookieManager.expireCookie(request, response, cookieName);

      // then
      String setCookieHeader = response.getHeader("Set-Cookie");
      assertThat(setCookieHeader).contains("Domain=portfolio.yeon.world");
    }

    @Test
    @DisplayName("호스트가 일치하는 도메인으로 끝나면 해당 도메인을 사용한다")
    void usesMatchingDomainWhenHostEndsWithDomain() {
      // given
      String cookieName = "test-cookie";
      request.setServerName("portfolio.yeon.world");
      given(jwtProps.authorizedCookieDomains()).willReturn(List.of("portfolio.yeon.world"));

      // when
      cookieManager.expireCookie(request, response, cookieName);

      // then
      String setCookieHeader = response.getHeader("Set-Cookie");
      assertThat(setCookieHeader).contains("Domain=portfolio.yeon.world");
    }

    @Test
    @DisplayName("설정된 쿠키 도메인이 없으면 예외를 던진다")
    void throwsExceptionWhenNoCookieDomainsConfigured() {
      // given
      String cookieName = "test-cookie";
      request.setServerName("other-domain.com");
      given(jwtProps.authorizedCookieDomains()).willReturn(List.of());

      // when & then
      assertThatThrownBy(() -> cookieManager.expireCookie(request, response, cookieName))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("No authorized cookie domains configured");
    }

    @Test
    @DisplayName("쿠키 도메인이 null이면 설정 예외를 던진다")
    void throwsExceptionWhenCookieDomainsIsNull() {
      // given
      String cookieName = "test-cookie";
      request.setServerName("other-domain.com");
      given(jwtProps.authorizedCookieDomains()).willReturn(null);

      // when & then
      assertThatThrownBy(() -> cookieManager.expireCookie(request, response, cookieName))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("No authorized cookie domains configured");
    }
  }
}
