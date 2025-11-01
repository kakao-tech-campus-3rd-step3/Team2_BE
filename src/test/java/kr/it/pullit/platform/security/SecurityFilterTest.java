package kr.it.pullit.platform.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import kr.it.pullit.modules.auth.exception.AuthErrorCode;
import kr.it.pullit.platform.security.jwt.JwtAuthenticator;
import kr.it.pullit.platform.security.jwt.JwtTokenProvider;
import kr.it.pullit.platform.security.jwt.exception.JwtAuthenticationEntryPoint;
import kr.it.pullit.platform.security.jwt.exception.JwtAuthenticationException;
import kr.it.pullit.support.annotation.MvcSliceTest;
import kr.it.pullit.support.apidocs.ProblemDetailTestUtils;
import kr.it.pullit.support.config.SecurityFilterTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@MvcSliceTest(controllers = SecurityFilterTest.TestController.class)
@Import({SecurityFilterTestConfig.class, JwtAuthenticationEntryPoint.class})
@DisplayName("보안 필터 통합 테스트")
class SecurityFilterTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private JwtAuthenticator jwtAuthenticator;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @RestController
  static class TestController {
    @GetMapping("/api/test/secure")
    @PreAuthorize("hasRole('MEMBER')")
    public String secureEndpoint() {
      return "SUCCESS";
    }
  }

  @Test
  @DisplayName("AUTH007: 유효하지 않은 액세스 토큰 요청 시, ApiDocs의 ExampleObject와 실제 응답이 일치한다")
  void shouldMatchApiDocsWhenTokenIsInvalid() throws Exception {
    // given
    when(jwtAuthenticator.authenticate(any()))
        .thenThrow(JwtAuthenticationException.from(AuthErrorCode.INVALID_ACCESS_TOKEN));

    // when & then
    mockMvc
        .perform(get("/api/test/secure").header("Authorization", "Bearer invalid-token"))
        .andExpect(
            ProblemDetailTestUtils.conformToApiDocs("/api/test/secure", "INVALID_ACCESS_TOKEN"));
  }
}
