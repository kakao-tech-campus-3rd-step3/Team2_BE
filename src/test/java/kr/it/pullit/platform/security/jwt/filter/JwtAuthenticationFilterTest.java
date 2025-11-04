package kr.it.pullit.platform.security.jwt.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.it.pullit.platform.security.jwt.JwtAuthenticator;
import kr.it.pullit.platform.security.jwt.JwtTokenProvider;
import kr.it.pullit.platform.security.jwt.PullitAuthenticationToken;
import kr.it.pullit.platform.security.jwt.exception.JwtAuthenticationEntryPoint;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

@MockitoUnitTest
@DisplayName("JwtAuthenticationFilter 단위 테스트")
class JwtAuthenticationFilterTest {

  @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private JwtAuthenticator jwtAuthenticator;

  @Mock private JwtAuthenticationEntryPoint entryPoint;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @DisplayName("유효한 토큰이 제공되면 인증 객체를 SecurityContext에 저장한다")
  @Test
  void doFilterInternal_withValidToken() throws ServletException, IOException {
    // given
    String token = "valid.token.string";
    PullitAuthenticationToken authentication =
        new PullitAuthenticationToken(1L, "test@test.com", null);

    when(jwtTokenProvider.resolveToken(request)).thenReturn(token);
    when(jwtAuthenticator.authenticate(token)).thenReturn(authentication);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(entryPoint);
  }

  @DisplayName("인증에 실패하면 SecurityContext를 비우고 entryPoint를 호출한다")
  @Test
  void doFilterInternal_withInvalidToken() throws ServletException, IOException {
    // given
    String token = "invalid.token.string";
    AuthenticationException exception = new BadCredentialsException("Invalid token");

    when(jwtTokenProvider.resolveToken(request)).thenReturn(token);
    when(jwtAuthenticator.authenticate(token)).thenThrow(exception);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(entryPoint).commence(request, response, exception);
    verify(filterChain, never()).doFilter(request, response);
  }

  @DisplayName("토큰이 없으면 인증 과정 없이 필터 체인을 계속 진행한다")
  @Test
  void doFilterInternal_withoutToken() throws ServletException, IOException {
    // given
    when(jwtTokenProvider.resolveToken(request)).thenReturn(null);
    when(jwtAuthenticator.authenticate(null)).thenReturn(null);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(entryPoint);
  }
}
