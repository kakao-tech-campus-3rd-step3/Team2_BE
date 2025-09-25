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
    Long kakaoId = (Long) attributes.get("id");

    Member member =
        memberPublicApi
            .findByKakaoId(kakaoId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "OAuth2 user not found in DB by kakaoId: " + kakaoId));

    AuthTokens authTokens = authService.issueAndSaveTokens(member.getId());

    cookieManager.addRefreshTokenCookie(request, response, authTokens.refreshToken());

    String targetUrl =
        UriComponentsBuilder.fromUriString(jwtProps.redirectUrl())
            .queryParam("accessToken", authTokens.accessToken())
            .build()
            .toUriString();

    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }
}
