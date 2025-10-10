package kr.it.pullit.modules.auth.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import kr.it.pullit.modules.auth.service.AuthService;
import kr.it.pullit.platform.security.exception.InvalidRefreshTokenException;
import kr.it.pullit.platform.security.jwt.JwtTokenPort;
import kr.it.pullit.platform.web.cookie.CookieManager;
import kr.it.pullit.shared.error.ErrorCode;
import kr.it.pullit.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@WithMockUser
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthService authService;

  @MockitoBean private JwtTokenPort jwtTokenPort;

  @MockitoBean private CookieManager cookieManager;

  @Nested
  @DisplayName("토큰 재발급 API [/auth/refresh]")
  class ReissueAccessToken {

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 요청 시 ProblemDetail 형식의 401 에러를 반환한다")
    void shouldReturnProblemDetailForInvalidToken() throws Exception {
      // given
      String invalidRefreshToken = "invalid-token";
      when(authService.reissueAccessToken(anyString()))
          .thenThrow(new InvalidRefreshTokenException());

      // when & then
      mockMvc
          .perform(post("/auth/refresh").cookie(new Cookie("refresh_token", invalidRefreshToken)))
          .andDo(print()) // 응답/요청 내용을 콘솔에 출력
          .andExpect(status().isUnauthorized())
          .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
          .andExpect(jsonPath("$.status").value(401))
          .andExpect(jsonPath("$.title").value("Unauthorized"))
          .andExpect(jsonPath("$.detail").value(ErrorCode.INVALID_REFRESH_TOKEN.getMessage()))
          .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()))
          .andExpect(jsonPath("$.instance").value("/auth/refresh"));
    }
  }
}
