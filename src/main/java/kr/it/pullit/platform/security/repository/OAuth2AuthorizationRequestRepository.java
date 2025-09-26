package kr.it.pullit.platform.security.repository;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.it.pullit.platform.security.handler.OAuth2AuthenticationSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  // Note: For simplicity, this implementation uses the session to store the request.
  // A cookie-based implementation could also be used for statelessness.
  // This example focuses on capturing the redirect_uri.

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    OAuth2AuthorizationRequest req =
        (OAuth2AuthorizationRequest)
            request.getSession().getAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    if (req == null) {
      log.error(
          "[AUTH_REQ_REPO] loadAuthorizationRequest: NOT FOUND. sessionId={}, incomingState={}",
          request.getSession().getId(),
          request.getParameter("state"));
    } else {
      log.debug(
          "[AUTH_REQ_REPO] loadAuthorizationRequest: FOUND. "
              + "sessionId={}, savedState={}, redirectUri={}",
          request.getSession().getId(),
          req.getState(),
          req.getRedirectUri());
    }
    return req;
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (authorizationRequest == null) {
      log.debug("AuthorizationRequest가 null입니다. 세션 속성을 제거합니다.");
      removeSessionAttributes(request);
      return;
    }

    log.debug(
        "[AUTH_REQ_REPO] AuthorizationRequest 저장 중. state={}, redirect_uri={}",
        authorizationRequest.getState(),
        authorizationRequest.getRedirectUri());
    request
        .getSession()
        .setAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, authorizationRequest);

    String redirectUriAfterLogin = request.getParameter("redirect_uri");
    log.debug("[AUTH_REQ_REPO] 요청에서 redirect_uri 파라미터 확인: {}", redirectUriAfterLogin);

    if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
      request
          .getSession()
          .setAttribute(
              OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY, redirectUriAfterLogin);
      log.debug(
          "[AUTH_REQ_REPO] redirect_uri '{}'를 세션 속성 '{}'에 저장했습니다.",
          redirectUriAfterLogin,
          OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY);
    }
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
    if (authorizationRequest != null) {
      this.removeSessionAttributes(request);
    }
    return authorizationRequest;
  }

  private void removeSessionAttributes(HttpServletRequest request) {
    request.getSession().removeAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
  }
}
