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
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.platform.security.jwt.JwtProps;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.web.cookie.CookieManager;
import kr.it.pullit.shared.error.exception.InvalidConfigurationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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

    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
    Map<String, Object> attributes = oauth2User.getAttributes();
    Object kakaoIdAttribute = attributes.get("id");
    if (!(kakaoIdAttribute instanceof Number kakaoIdNumber)) {
      throw new IllegalArgumentException("Kakao OAuth 응답에 숫자형 id가 없습니다.");
    }
    Long kakaoId = kakaoIdNumber.longValue();

    Member member =
        memberPublicApi
            .findByKakaoId(kakaoId)
            .orElseThrow(() -> MemberNotFoundException.byKakaoId(kakaoId));

    AuthTokens authTokens = authService.issueAndSaveTokens(member.getId());

    String targetUrl = determineTargetUrl(request, authTokens.accessToken());
    String cookieDomain = determineBackendCookieDomain(request);

    cookieManager.addRefreshTokenCookie(response, authTokens.refreshToken(), cookieDomain);

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  private String determineTargetUrl(HttpServletRequest request, String accessToken) {
    String redirectUri = (String) request.getSession().getAttribute(REDIRECT_URI_SESSION_KEY);
    request.getSession().removeAttribute(REDIRECT_URI_SESSION_KEY);

    String validatedUri =
        Optional.ofNullable(redirectUri)
            .filter(this::isAuthorizedRedirectUri)
            .orElseGet(
                () -> {
                  return getDefaultRedirectUri();
                });

    return UriComponentsBuilder.fromUriString(validatedUri)
        .queryParam("accessToken", accessToken)
        .build()
        .toUriString();
  }

  private boolean isAuthorizedRedirectUri(String uri) {
    try {
      URI clientRedirectUri = URI.create(uri).normalize();
      return jwtProps.authorizedRedirectUris().stream()
          .map(URI::create)
          .map(URI::normalize)
          .anyMatch(clientRedirectUri::equals);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  private String getDefaultRedirectUri() {
    return jwtProps.authorizedRedirectUris().getFirst();
  }

  private String determineBackendCookieDomain(HttpServletRequest request) {
    String host = request.getServerName();
    if (host == null) {
      throw InvalidConfigurationException.withMessage("요청에서 서버 호스트를 추출할 수 없습니다.");
    }

    // 로컬 백엔드(개발): Domain 미지정(host-only) 쿠키로 발급해야 브라우저가 수용
    if ("localhost".equalsIgnoreCase(host)) {
      return null;
    }

    return jwtProps.authorizedCookieDomains().stream()
        .filter(domain -> matchesCookieDomain(host, domain))
        .findFirst()
        .orElseThrow(() -> InvalidConfigurationException.withMessage("쿠키 도메인 설정이 없습니다: " + host));
  }

  private boolean matchesCookieDomain(String host, String configuredDomain) {
    String normalizedDomain =
        configuredDomain.startsWith(".") ? configuredDomain.substring(1) : configuredDomain;
    return host.equalsIgnoreCase(normalizedDomain)
        || host.toLowerCase().endsWith("." + normalizedDomain.toLowerCase());
  }

  private String getDefaultCookieDomain() {
    List<String> domains = jwtProps.authorizedCookieDomains();
    if (domains == null || domains.isEmpty()) {
      throw InvalidConfigurationException.withMessage(
          "누락된 환경변수 : authorized-cookie-domains, 설정된 쿠키 도메인 :" + domains);
    }
    return domains.getFirst();
  }
}
