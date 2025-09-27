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
  public static final String REFRESH_TOKEN_COOKIE_PATH = "/auth/refresh";

  private final JwtProps jwtProps;

  public void addRefreshTokenCookie(
      HttpServletRequest request,
      HttpServletResponse response,
      String refreshToken,
      String domain) {
    long maxAge = jwtProps.refreshTokenExpirationDays().getSeconds();
    ResponseCookie cookie = createRefreshTokenCookie(request, refreshToken, maxAge, domain);
    log.info("Generated Refresh Token Cookie string: {}", cookie.toString());
    response.addHeader("Set-Cookie", cookie.toString());
  }

  public void removeRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response) {
    String domain = determineDomainFromRequest(request);
    ResponseCookie cookie = createRefreshTokenCookie(request, "", 0, domain);
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private ResponseCookie createRefreshTokenCookie(
      HttpServletRequest request, String value, long maxAge, String domain) {
    ResponseCookie.ResponseCookieBuilder cookieBuilder =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(true)
            .path(REFRESH_TOKEN_COOKIE_PATH)
            .maxAge(maxAge)
            .sameSite("None");

    if (domain != null && !domain.isBlank()) {
      cookieBuilder.domain(domain);
    }

    return cookieBuilder.build();
  }

  private String determineDomainFromRequest(HttpServletRequest request) {
    String host = request.getServerName();
    if (host == null) {
      log.warn("요청에서 호스트를 추출할 수 없어 기본 쿠키 도메인을 사용합니다.");
      return getDefaultCookieDomain();
    }

    return jwtProps.authorizedCookieDomains().stream()
        .filter(host::endsWith)
        .findFirst()
        .orElseGet(
            () -> {
              log.warn("호스트 '{}'에 일치하는 쿠키 도메인 설정이 없어 기본 쿠키 도메인을 사용합니다.", host);
              return getDefaultCookieDomain();
            });
  }

  private String getDefaultCookieDomain() {
    List<String> domains = jwtProps.authorizedCookieDomains();
    if (domains == null || domains.isEmpty()) {
      log.error("설정된 쿠키 도메인이 없습니다.");
      throw new IllegalStateException("No authorized cookie domains configured");
    }
    return domains.get(0);
  }
}
