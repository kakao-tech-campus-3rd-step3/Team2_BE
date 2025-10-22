package kr.it.pullit.platform.security.jwt.filter;

import java.io.IOException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.it.pullit.platform.security.jwt.handler.LocalAuthenticationHandler;
import lombok.RequiredArgsConstructor;

@Profile("local")
@Component
@RequiredArgsConstructor
public class DevAuthenticationFilter extends OncePerRequestFilter {

  private final LocalAuthenticationHandler localAuthenticationHandler;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    localAuthenticationHandler.authenticate(request);

    filterChain.doFilter(request, response);
  }
}
