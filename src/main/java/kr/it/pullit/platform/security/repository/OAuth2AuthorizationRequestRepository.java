package kr.it.pullit.platform.security.repository;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.it.pullit.platform.security.handler.OAuth2AuthenticationSuccessHandler;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";

  // Note: For simplicity, this implementation uses the session to store the request.
  // A cookie-based implementation could also be used for statelessness.
  // This example focuses on capturing the redirect_uri.

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return (OAuth2AuthorizationRequest)
        request.getSession().getAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
  }

  @Override
  public void saveAuthorizationRequest(
      OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (authorizationRequest == null) {
      removeSessionAttributes(request);
      return;
    }

    request
        .getSession()
        .setAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, authorizationRequest);

    String redirectUriAfterLogin = request.getParameter("redirect_uri");
    if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
      request
          .getSession()
          .setAttribute(
              OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY, redirectUriAfterLogin);
    } else {
      request
          .getSession()
          .removeAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY);
    }
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
      HttpServletRequest request, HttpServletResponse response) {
    OAuth2AuthorizationRequest authorizationRequest = this.loadAuthorizationRequest(request);
    this.removeSessionAttributes(request);
    return authorizationRequest;
  }

  private void removeSessionAttributes(HttpServletRequest request) {
    request.getSession().removeAttribute(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    request
        .getSession()
        .removeAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY);
  }
}
