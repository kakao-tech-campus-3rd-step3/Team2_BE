package kr.it.pullit.platform.security.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.HttpSession;
import kr.it.pullit.platform.security.handler.OAuth2AuthenticationSuccessHandler;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@MockitoUnitTest
@DisplayName("OAuth2AuthorizationRequestRepository 단위 테스트")
class OAuth2AuthorizationRequestRepositoryTest {

  @InjectMocks private OAuth2AuthorizationRequestRepository repository;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Nested
  @DisplayName("loadAuthorizationRequest")
  class DescribeLoadAuthorizationRequest {

    @Test
    @DisplayName("세션에 저장된 인증 요청을 로드한다")
    void loadsAuthorizationRequestFromSession() {
      // given
      var authRequest = mock(OAuth2AuthorizationRequest.class);
      HttpSession session = request.getSession();
      session.setAttribute(
          OAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
          authRequest);

      // when
      OAuth2AuthorizationRequest result = repository.loadAuthorizationRequest(request);

      // then
      assertThat(result).isEqualTo(authRequest);
    }

    @Test
    @DisplayName("세션에 인증 요청이 없으면 null을 반환한다")
    void returnsNullWhenNoAuthorizationRequestInSession() {
      // when
      OAuth2AuthorizationRequest result = repository.loadAuthorizationRequest(request);

      // then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("saveAuthorizationRequest")
  class DescribeSaveAuthorizationRequest {

    @Test
    @DisplayName("인증 요청을 세션에 저장한다")
    void savesAuthorizationRequestToSession() {
      // given
      var authRequest = mock(OAuth2AuthorizationRequest.class);

      // when
      repository.saveAuthorizationRequest(authRequest, request, response);

      // then
      HttpSession session = request.getSession();
      OAuth2AuthorizationRequest saved =
          (OAuth2AuthorizationRequest)
              session.getAttribute(
                  OAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
      assertThat(saved).isEqualTo(authRequest);
    }

    @Test
    @DisplayName("redirect_uri 파라미터가 있으면 세션에 저장한다")
    void savesRedirectUriToSessionWhenParameterExists() {
      // given
      var authRequest = mock(OAuth2AuthorizationRequest.class);
      String redirectUri = "https://portfolio.yeon.world/callback";
      request.setParameter("redirect_uri", redirectUri);

      // when
      repository.saveAuthorizationRequest(authRequest, request, response);

      // then
      HttpSession session = request.getSession();
      String savedRedirectUri =
          (String)
              session.getAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY);
      assertThat(savedRedirectUri).isEqualTo(redirectUri);
    }

    @Test
    @DisplayName("redirect_uri 파라미터가 없으면 세션에 저장하지 않는다")
    void doesNotSaveRedirectUriWhenParameterIsMissing() {
      // given
      var authRequest = mock(OAuth2AuthorizationRequest.class);

      // when
      HttpSession session = request.getSession();
      session.setAttribute(
          OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY, "stale-uri");
      repository.saveAuthorizationRequest(authRequest, request, response);

      // then
      String savedRedirectUri =
          (String)
              session.getAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY);
      assertThat(savedRedirectUri).isNull();
    }

    @Test
    @DisplayName("redirect_uri 파라미터가 빈 문자열이면 세션에 저장하지 않는다")
    void doesNotSaveRedirectUriWhenParameterIsBlank() {
      // given
      var authRequest = mock(OAuth2AuthorizationRequest.class);
      request.setParameter("redirect_uri", "");

      // when
      repository.saveAuthorizationRequest(authRequest, request, response);

      // then
      HttpSession session = request.getSession();
      String savedRedirectUri =
          (String)
              session.getAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY);
      assertThat(savedRedirectUri).isNull();
    }

    @Test
    @DisplayName("인증 요청이 null이면 세션 속성을 제거한다")
    void removesSessionAttributeWhenAuthorizationRequestIsNull() {
      // given
      var authRequest = mock(OAuth2AuthorizationRequest.class);
      HttpSession session = request.getSession();
      session.setAttribute(
          OAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
          authRequest);

      // when
      repository.saveAuthorizationRequest(null, request, response);

      // then
      Object saved =
          session.getAttribute(
              OAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
      assertThat(saved).isNull();
    }
  }

  @Nested
  @DisplayName("removeAuthorizationRequest")
  class DescribeRemoveAuthorizationRequest {

    @Test
    @DisplayName("세션에서 인증 요청을 제거하고 반환한다")
    void removesAndReturnsAuthorizationRequest() {
      // given
      var authRequest = mock(OAuth2AuthorizationRequest.class);
      HttpSession session = request.getSession();
      session.setAttribute(
          OAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
          authRequest);
      session.setAttribute(
          OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY, "saved-uri");

      // when
      OAuth2AuthorizationRequest result = repository.removeAuthorizationRequest(request, response);

      // then
      assertThat(result).isEqualTo(authRequest);
      Object saved =
          session.getAttribute(
              OAuth2AuthorizationRequestRepository.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
      assertThat(saved).isNull();
      assertThat(session.getAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY))
          .isNull();
    }

    @Test
    @DisplayName("세션에 인증 요청이 없으면 null을 반환한다")
    void returnsNullWhenNoAuthorizationRequestInSession() {
      // when
      OAuth2AuthorizationRequest result = repository.removeAuthorizationRequest(request, response);

      // then
      assertThat(result).isNull();
    }
  }
}
