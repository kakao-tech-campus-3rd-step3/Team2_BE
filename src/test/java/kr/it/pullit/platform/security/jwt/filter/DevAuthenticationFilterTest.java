package kr.it.pullit.platform.security.jwt.filter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.it.pullit.platform.security.jwt.handler.LocalAuthenticationHandler;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;

@MockitoUnitTest
@DisplayName("DevAuthenticationFilter 단위 테스트")
class DevAuthenticationFilterTest {

  @InjectMocks private DevAuthenticationFilter devAuthenticationFilter;

  @Mock private LocalAuthenticationHandler localAuthenticationHandler;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @DisplayName("로컬 프로필에서 DevAuthenticationFilter는 LocalAuthenticationHandler를 통해 인증을 수행한다")
  @Test
  void doFilterInternal() throws ServletException, IOException {
    // given
    HttpServletRequest processedRequest = new MockHttpServletRequest();
    when(localAuthenticationHandler.authenticate(request)).thenReturn(processedRequest);

    // when
    devAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(localAuthenticationHandler).authenticate(request);
    verify(filterChain).doFilter(processedRequest, response);
  }
}
