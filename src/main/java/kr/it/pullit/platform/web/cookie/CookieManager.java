package kr.it.pullit.platform.web.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import kr.it.pullit.platform.security.jwt.JwtProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieManager {

  private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  public static final String DEFAULT_REFRESH_TOKEN_COOKIE_PATH = "/auth/refresh";

  private final JwtProps jwtProps;

  public void addRefreshTokenCookie(
      HttpServletResponse response, String refreshToken, String domain) {
    long maxAge = jwtProps.refreshTokenExpirationDays().getSeconds();
    ResponseCookie cookie = createRefreshTokenCookie(refreshToken, maxAge, domain);
    log.debug("리프레시 토큰 쿠키를 발급했습니다. domain={}, path={}", domain, cookie.getPath());
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private ResponseCookie createRefreshTokenCookie(String value, long maxAge, String domain) {
    ResponseCookie.ResponseCookieBuilder cookieBuilder =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(true)
            .path(getRefreshTokenCookiePath())
            .maxAge(maxAge)
            .sameSite("None");

    if (domain != null && !domain.isBlank()) {
      cookieBuilder.domain(domain);
    }

    return cookieBuilder.build();
  }

  public void expireCookie(
      HttpServletRequest request, HttpServletResponse response, String cookieName) {
    String domain = determineDomainFromRequest(request);
    String path = determinePathForCookie(cookieName);

    ResponseCookie.ResponseCookieBuilder cookieBuilder =
        ResponseCookie.from(cookieName, "")
            .httpOnly(true)
            .secure(true)
            .path(path)
            .maxAge(0)
            .sameSite("None");

    if (domain != null && !domain.isBlank()) {
      cookieBuilder.domain(domain);
    }

    response.addHeader("Set-Cookie", cookieBuilder.build().toString());
  }

  private String determinePathForCookie(String cookieName) {
    if (REFRESH_TOKEN_COOKIE_NAME.equals(cookieName)) {
      return getRefreshTokenCookiePath();
    }
    return "/";
  }

  private String getRefreshTokenCookiePath() {
    String configuredPath = jwtProps.refreshTokenCookiePath();
    return configuredPath == null || configuredPath.isBlank()
        ? DEFAULT_REFRESH_TOKEN_COOKIE_PATH
        : configuredPath;
  }

  private String determineDomainFromRequest(HttpServletRequest request) {
    String host = request.getServerName();
    if (host == null) {
      log.warn("요청에서 호스트를 추출할 수 없어 기본 쿠키 도메인을 사용합니다.");
      return getDefaultCookieDomain();
    }
    if ("localhost".equalsIgnoreCase(host)) {
      return null;
    }

    List<String> domains = getAuthorizedCookieDomains();
    return domains.stream()
        .filter(domain -> matchesCookieDomain(host, domain))
        .findFirst()
        .orElseGet(
            () -> {
              log.warn("호스트 '{}'에 일치하는 쿠키 도메인 설정이 없어 기본 쿠키 도메인을 사용합니다.", host);
              return getDefaultCookieDomain();
            });
  }

  private String getDefaultCookieDomain() {
    return getAuthorizedCookieDomains().getFirst();
  }

  private List<String> getAuthorizedCookieDomains() {
    List<String> domains = jwtProps.authorizedCookieDomains();
    if (domains == null || domains.isEmpty()) {
      log.error("설정된 쿠키 도메인이 없습니다.");
      throw new IllegalStateException("No authorized cookie domains configured");
    }
    return domains;
  }

  private boolean matchesCookieDomain(String host, String configuredDomain) {
    String normalizedDomain =
        configuredDomain.startsWith(".") ? configuredDomain.substring(1) : configuredDomain;
    return host.equalsIgnoreCase(normalizedDomain)
        || host.toLowerCase().endsWith("." + normalizedDomain.toLowerCase());
  }
}
