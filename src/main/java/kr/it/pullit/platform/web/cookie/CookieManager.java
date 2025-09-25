package kr.it.pullit.platform.web.cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    // Note: When removing a cookie, the domain must also match.
    // This might need enhancement if cookies can be set on multiple domains.
    // For now, it uses the default domain from props.
    ResponseCookie cookie = createRefreshTokenCookie(request, "", 0, jwtProps.cookieDomain());
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private ResponseCookie createRefreshTokenCookie(
      HttpServletRequest request, String value, long maxAge, String domain) {
    ResponseCookie.ResponseCookieBuilder cookieBuilder =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(request.isSecure())
            .path("/")
            .maxAge(maxAge)
            .sameSite(request.isSecure() ? "None" : "Lax");

    if (domain != null && !domain.isBlank()) {
      cookieBuilder.domain(domain);
    }

    return cookieBuilder.build();
  }
}
