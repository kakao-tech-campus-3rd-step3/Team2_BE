package kr.it.pullit.platform.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.it.pullit.platform.security.jwt.PullitAuthenticationToken;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Profile("local")
@Component
public class DevAuthenticationFilter extends OncePerRequestFilter {

  private static final Long DEFAULT_MEMBER_ID = 1L;
  private static final String DEFAULT_MEMBER_EMAIL = "dev-user@pullit.kr";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    PullitAuthenticationToken token =
        new PullitAuthenticationToken(DEFAULT_MEMBER_ID, DEFAULT_MEMBER_EMAIL, null);
    context.setAuthentication(token);
    SecurityContextHolder.setContext(context);

    filterChain.doFilter(request, response);
  }
}
