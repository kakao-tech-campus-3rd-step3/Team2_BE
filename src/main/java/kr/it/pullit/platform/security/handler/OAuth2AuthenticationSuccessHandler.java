package kr.it.pullit.platform.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import kr.it.pullit.modules.auth.service.AuthTokenService;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.platform.security.config.JwtProps;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final AuthTokenService authTokenService;
  private final JwtProps jwtProps;
  private final MemberRepository memberRepository;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

    Map<String, Object> attributes = oauth2User.getAttributes();
    Long kakaoId = (Long) attributes.get("id");

    Member member =
        memberRepository
            .findByKakaoId(kakaoId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "OAuth2 user not found in DB by kakaoId: " + kakaoId));

    // Access Token과 Refresh Token을 생성하고 DB에 저장합니다.
    AuthTokens authTokens = authTokenService.issueAndSaveTokens(member);

    // Refresh Token을 HttpOnly, Secure 쿠키에 담아 클라이언트에 전달합니다.
    addRefreshTokenToCookie(request, response, authTokens.refreshToken());

    // Access Token을 URL에 포함시키지 않고 리다이렉트합니다.
    String targetUrl =
        UriComponentsBuilder.fromUriString(jwtProps.redirectUrl()).build().toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private void addRefreshTokenToCookie(
      HttpServletRequest request, HttpServletResponse response, String refreshToken) {
    long maxAge = jwtProps.refreshTokenExpirationDays() * 24 * 60 * 60;
    boolean isSecure = request.isSecure();
    String domain = getDomainFromRedirectUrl();

    ResponseCookie cookie =
        ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(maxAge)
            .domain(domain)
            .sameSite(isSecure ? "None" : "Lax")
            .build();

    response.addHeader("Set-Cookie", cookie.toString());
    log.info("Refresh token cookie added to response: {}", cookie);
  }

  private String getDomainFromRedirectUrl() {
    String redirectUrl = jwtProps.redirectUrl();
    log.info("Attempting to extract domain from redirect URL: '{}'", redirectUrl);

    try {
      URI redirectUri = new URI(redirectUrl);
      String host = redirectUri.getHost();
      log.info("Successfully extracted domain for cookie: '{}'", host);
      return host;
    } catch (URISyntaxException e) {
      log.error("Invalid redirect URL syntax: {}", redirectUrl, e);
      // fallback to a sensible default or throw an exception
      return null;
    }
  }
}
