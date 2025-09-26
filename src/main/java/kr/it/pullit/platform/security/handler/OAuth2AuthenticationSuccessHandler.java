package kr.it.pullit.platform.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.security.jwt.JwtProps;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.web.cookie.CookieManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    log.debug(
        "[SUCCESS_HANDLER] onAuthenticationSuccess 실행됨. principal={}",
        authentication.getPrincipal());

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

    AuthTokens authTokens = authService.issueAndSaveTokens(member.getId());

    String targetUrl = determineTargetUrl(request, authTokens.accessToken());
    String cookieDomain = determineBackendCookieDomain(request);

    cookieManager.addRefreshTokenCookie(request, response, authTokens.refreshToken(), cookieDomain);

    log.info("모든 `Set-Cookie` 헤더: {}", response.getHeaders("Set-Cookie"));
    log.info("리다이렉션 주소: {}", targetUrl);

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private String determineTargetUrl(HttpServletRequest request, String accessToken) {
    String redirectUri = (String) request.getSession().getAttribute(REDIRECT_URI_SESSION_KEY);
    request.getSession().removeAttribute(REDIRECT_URI_SESSION_KEY);

    String validatedUri =
        Optional.ofNullable(redirectUri)
            .filter(uri -> isAuthorizedRedirectUri(uri))
            .orElseGet(
                () -> {
                  log.warn("세션에 유효한 리다이렉션 URI가 없습니다. 기본 URI로 대체합니다.");
                  return getDefaultRedirectUri();
                });

    return UriComponentsBuilder.fromUriString(validatedUri)
        .queryParam("accessToken", accessToken)
        .build()
        .toUriString();
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);

    return jwtProps.authorizedRedirectUris().stream()
        .anyMatch(
            authorizedRedirectUri -> {
              URI authorizedUri = URI.create(authorizedRedirectUri);
              return authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                  && authorizedUri.getPort() == clientRedirectUri.getPort();
            });
  }

  private String getDefaultRedirectUri() {
    return jwtProps.authorizedRedirectUris().get(0);
  }

  private String determineBackendCookieDomain(HttpServletRequest request) {
    String host = request.getServerName();
    if (host == null) {
      log.warn("요청에서 서버 호스트를 추출할 수 없어 기본 쿠키 도메인을 사용합니다.");
      return getDefaultCookieDomain();
    }

    // 로컬 백엔드(개발): Domain 미지정(host-only) 쿠키로 발급해야 브라우저가 수용
    if ("localhost".equalsIgnoreCase(host)) {
      return null;
    }

    if (host.endsWith("pull.it.kr")) {
      return ".pull.it.kr";
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
