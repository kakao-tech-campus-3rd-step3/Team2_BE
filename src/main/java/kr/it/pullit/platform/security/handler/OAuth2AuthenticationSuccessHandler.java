package kr.it.pullit.platform.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.it.pullit.boot.properties.AppProps;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.security.jwt.JwtProps;
import kr.it.pullit.platform.security.jwt.JwtTokenPort;
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

  private final JwtTokenPort jwtTokenPort;
  private final MemberPublicApi memberPublicApi;
  private final JwtProps jwtProps;
  private final AppProps appProps;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    Long kakaoId = oAuth2User.getAttribute("id");

    Member member =
        memberPublicApi
            .findByKakaoId(kakaoId)
            .orElseThrow(
                () -> new IllegalStateException("OAuth2 사용자를 찾을 수 없습니다. KakaoId: " + kakaoId));

    String refreshToken = jwtTokenPort.createRefreshToken(member);
    addRefreshTokenToCookie(response, refreshToken);

    String targetUrl =
        UriComponentsBuilder.fromUriString(jwtProps.redirectUrl()).build().toUriString();

    clearAuthenticationAttributes(request);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
    long maxAge = jwtProps.refreshTokenExpirationDays().toSeconds();

    boolean isSecure = "https".equalsIgnoreCase(appProps.scheme());

    ResponseCookie cookie =
        ResponseCookie.from(jwtProps.refreshTokenCookieName(), refreshToken)
            .httpOnly(true)
            .secure(isSecure)
            .path("/")
            .maxAge(maxAge)
            .sameSite("None")
            .build();

    response.addHeader("Set-Cookie", cookie.toString());
    log.info("Refresh token cookie가 응답에 추가되었습니다. (Secure={})", isSecure);
  }
}
