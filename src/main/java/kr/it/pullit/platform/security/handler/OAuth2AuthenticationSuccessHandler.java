package kr.it.pullit.platform.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.security.jwt.JwtProps;
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

  private static final String COOKIE_DOMAIN = "pull.it.kr";

  private final AuthService authService;
  private final JwtProps jwtProps;
  private final MemberPublicApi memberPublicApi;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

    Map<String, Object> attributes = oauth2User.getAttributes();
    Long kakaoId = (Long) attributes.get("id");

    Member member =
        memberPublicApi
            .findByKakaoId(kakaoId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "OAuth2 user not found in DB by kakaoId: " + kakaoId));

    AuthTokens authTokens = authService.issueAndSaveTokens(member);

    addRefreshTokenToCookie(request, response, authTokens.refreshToken());

    String targetUrl =
        UriComponentsBuilder.fromUriString(jwtProps.redirectUrl()).build().toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private void addRefreshTokenToCookie(
      HttpServletRequest request, HttpServletResponse response, String refreshToken) {
    long maxAge = jwtProps.refreshTokenExpirationDays().getSeconds();
    boolean isSecure = request.isSecure();

    ResponseCookie cookie =
        ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(maxAge)
            .domain(COOKIE_DOMAIN)
            .sameSite(isSecure ? "None" : "Lax")
            .build();

    response.addHeader("Set-Cookie", cookie.toString());
    log.info("Refresh token cookie added to response: {}", cookie);
  }
}
