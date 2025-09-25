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
      HttpServletRequest request, HttpServletResponse response, String refreshToken) {
    long maxAge = jwtProps.refreshTokenExpirationDays().getSeconds();
    ResponseCookie cookie = createRefreshTokenCookie(request, refreshToken, maxAge);
    log.info("Generated Refresh Token Cookie string: {}", cookie.toString());
    response.addHeader("Set-Cookie", cookie.toString());
  }

  public void removeRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response) {
    ResponseCookie cookie = createRefreshTokenCookie(request, "", 0);
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private ResponseCookie createRefreshTokenCookie(
      HttpServletRequest request, String value, long maxAge) {
    ResponseCookie.ResponseCookieBuilder cookieBuilder =
        ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(request.isSecure())
            .path("/")
            .maxAge(maxAge)
            .sameSite(request.isSecure() ? "None" : "Lax");

    if (jwtProps.cookieDomain() != null && !jwtProps.cookieDomain().isBlank()) {
      cookieBuilder.domain(jwtProps.cookieDomain());
    }

    return cookieBuilder.build();
  }
}
