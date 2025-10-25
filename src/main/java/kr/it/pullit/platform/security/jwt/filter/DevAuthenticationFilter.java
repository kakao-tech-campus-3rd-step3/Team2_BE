package kr.it.pullit.platform.security.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.it.pullit.platform.security.jwt.handler.LocalAuthenticationHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Profile("local")
@Component
@RequiredArgsConstructor
public class DevAuthenticationFilter extends OncePerRequestFilter {

  private final LocalAuthenticationHandler localAuthenticationHandler;

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    HttpServletRequest processedRequest = localAuthenticationHandler.authenticate(request);

    filterChain.doFilter(processedRequest, response);
  }
}
