package kr.it.pullit.modules.auth.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import kr.it.pullit.modules.auth.exception.InvalidRefreshTokenException;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.platform.security.jwt.JwtTokenProvider;
import kr.it.pullit.platform.security.jwt.filter.JwtAuthenticationFilter;
import kr.it.pullit.platform.web.cookie.CookieManager;
import kr.it.pullit.support.annotation.MvcSliceTest;
import kr.it.pullit.support.apidocs.ProblemDetailTestUtils;
import kr.it.pullit.support.config.TestSecurityConfig;
import kr.it.pullit.support.security.WithMockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@MvcSliceTest(
    controllers = AuthController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(TestSecurityConfig.class)
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CookieManager cookieManager;

  @MockitoBean private AuthService authService;

  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @Nested
  @DisplayName("토큰 재발급 API [/auth/refresh]")
  class ReissueAccessToken {

    @Test
    @DisplayName("AUTH006: 유효하지 않은 리프레시 토큰 요청 시, ApiDocs의 ExampleObject와 실제 응답이 일치한다")
    void shouldMatchApiDocsWhenReissueWithInvalidRefreshToken() throws Exception {
      // given
      String invalidRefreshToken = "invalid-token";
      when(authService.reissueAccessToken(anyString()))
          .thenThrow(InvalidRefreshTokenException.by());

      // when & then
      mockMvc
          .perform(post("/auth/refresh").cookie(new Cookie("refresh_token", invalidRefreshToken)))
          .andDo(print()) // 요청/응답 내용을 콘솔에 출력합니다.
          .andExpect(
              ProblemDetailTestUtils.conformToApiDocs("/auth/refresh", "INVALID_REFRESH_TOKEN"));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 요청 시, 401 응답과 AUTH006 코드를 반환한다")
    void shouldReturnUnauthorizedWhenReissueWithInvalidRefreshToken() throws Exception {
      // given
      doThrow(InvalidRefreshTokenException.by()).when(authService).reissueAccessToken(any());

      // when & then
      mockMvc
          .perform(post("/auth/refresh").cookie(new Cookie("refresh_token", "invalid-token")))
          .andExpectAll(
              ProblemDetailTestUtils.conformToApiDocs("/auth/refresh", "INVALID_REFRESH_TOKEN"));
    }
  }

  @Nested
  @DisplayName("로그아웃 API [/auth/logout]")
  class Logout {
    @Test
    @WithMockMember
    @DisplayName("인증된 사용자가 로그아웃 요청 시, 204 No Content를 반환한다")
    void shouldReturnNoContentForAuthenticatedUser() throws Exception {
      // given

      // when & then
      mockMvc.perform(post("/auth/logout")).andExpect(status().isNoContent());

      // verify
      verify(authService).logout(1L);
    }
  }
}
