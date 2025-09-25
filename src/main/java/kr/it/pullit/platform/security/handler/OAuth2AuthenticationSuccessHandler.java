package kr.it.pullit.platform.security.handler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.security.jwt.JwtProps;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.web.cookie.CookieManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  public static final String REDIRECT_URI_SESSION_KEY = "redirect_uri_after_login";

  private final AuthService authService;
  private final JwtProps jwtProps;
  private final MemberPublicApi memberPublicApi;
  private final CookieManager cookieManager;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    Map<String, Object> attributes = oauth2User.getAttributes();
    Long kakaoId = (Long) attributes.get("id");

    Member member = memberPublicApi.findByKakaoId(kakaoId).orElseThrow(
        () -> new IllegalStateException("OAuth2 user not found in DB by kakaoId: " + kakaoId));

    AuthTokens authTokens = authService.issueAndSaveTokens(member.getId());

    String baseRedirectUri = getAndClearRedirectUriFromSession(request);

    String cookieDomain = createCookieDomainFrom(baseRedirectUri);

    cookieManager.addRefreshTokenCookie(request, response, authTokens.refreshToken(), cookieDomain);

    String targetUrl = createTargetUrlWithToken(baseRedirectUri, authTokens.accessToken());

    log.info("All `Set-Cookie` headers: {}", response.getHeaders("Set-Cookie"));
    log.info("Redirecting to: {}", targetUrl);

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  /**
   * OAuth2 인증 절차가 시작될 때 저장해둔 리다이렉션 URI를 세션에서 가져옵니다. 사용 후에는 재사용을 방지하기 위해 세션에서 해당 속성을 제거합니다.
   *
   * @param request 현재 HTTP 요청
   * @return 클라이언트가 요청한 리다이렉션 URI. 세션에 값이 없으면 기본 URI를 반환합니다.
   */
  private String getAndClearRedirectUriFromSession(HttpServletRequest request) {
    String redirectUri = (String) request.getSession().getAttribute(REDIRECT_URI_SESSION_KEY);
    request.getSession().removeAttribute(REDIRECT_URI_SESSION_KEY);

    if (redirectUri == null) {
      log.warn("세션에서 리다이렉션 URI를 찾지 못했습니다. 기본값으로 대체합니다.");
      return jwtProps.redirectUrl();
    }
    return redirectUri;
  }

  /**
   * 리다이렉션 URI의 호스트를 기반으로 쿠키 도메인을 생성
   *
   * @param redirectUri 호스트를 추출할 기준 URI
   * @return 쿠키 도메인
   */
  private String createCookieDomainFrom(String redirectUri) {
    try {
      String host = new URI(redirectUri).getHost();

      if (host == null) {
        log.warn("리다이렉션 URI '{}'에서 호스트를 추출할 수 없어 기본 쿠키 도메인을 사용합니다.", redirectUri);
        return jwtProps.cookieDomain();
      }

      if ("localhost".equalsIgnoreCase(host)) {
        return "localhost";
      }

      // pull.it.kr 및 모든 서브도메인(www, local 등)에 대해 항상 최상위 도메인으로 쿠키를 설정합니다.
      if (host.endsWith("pull.it.kr")) {
        return ".pull.it.kr";
      }

      log.warn("리다이렉션 URI 호스트 '{}'가 예상된 개발 또는 운영 도메인이 아닙니다. 기본값으로 대체합니다.", host);
      return jwtProps.cookieDomain();

    } catch (URISyntaxException e) {
      log.error("잘못된 리다이렉션 URI '{}'가 전달되어 쿠키 도메인을 생성할 수 없습니다.", redirectUri, e);
      return jwtProps.cookieDomain();
    }
  }

  /**
   * 기본 리다이렉션 URI에 액세스 토큰을 쿼리 파라미터로 추가
   *
   * @param baseRedirectUri 최종 목적지 URI (예: "https://pull.it.kr/login-success")
   * @param accessToken JWT 액세스 토큰
   * @return 토큰이 추가된 최종 URL
   */
  private String createTargetUrlWithToken(String baseRedirectUri, String accessToken) {
    return UriComponentsBuilder.fromUriString(baseRedirectUri)
        .queryParam("accessToken", accessToken).build().toUriString();
  }
}
