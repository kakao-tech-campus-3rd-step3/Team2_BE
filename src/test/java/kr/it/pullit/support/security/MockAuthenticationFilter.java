package kr.it.pullit.support.security;

import com.auth0.jwt.JWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.it.pullit.platform.security.jwt.PullitAuthenticationToken;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Profile("mock-auth")
public class MockAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    // 테스트용으로 사용할 가짜 memberId와 email
    Long mockMemberId = 1L;
    String mockEmail = "testuser@pullit.kr";

    // 가짜 토큰이지만, PullitAuthenticationToken 생성자에 필요하므로 최소한의 형태로 생성
    var mockDecodedJwt =
        JWT.decode(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.Et9HFtf9R3GEMA0I4L7EMV5Y343AdcU_oNaMSGYcLGk");

    PullitAuthenticationToken authentication =
        new PullitAuthenticationToken(mockMemberId, mockEmail, mockDecodedJwt);

    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }
}
