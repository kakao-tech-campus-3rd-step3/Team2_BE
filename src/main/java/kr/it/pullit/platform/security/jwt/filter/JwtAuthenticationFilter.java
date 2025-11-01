package kr.it.pullit.platform.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import kr.it.pullit.platform.security.jwt.AuthenticationResult;
import kr.it.pullit.platform.security.jwt.JwtAuthenticator;
import kr.it.pullit.platform.security.jwt.JwtTokenProvider;
import kr.it.pullit.platform.security.jwt.exception.TokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtAuthenticator jwtAuthenticator;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    try {
      authenticateRequest(request);
    } catch (TokenException e) {
      handleAuthenticationFailure(response, e);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private void authenticateRequest(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    AuthenticationResult authResult = jwtAuthenticator.authenticate(token);

    authResult.getAuthentication().ifPresent(SecurityContextHolder.getContext()::setAuthentication);
  }

  private void handleAuthenticationFailure(HttpServletResponse response, TokenException e)
      throws IOException {
    log.warn(
        "[JwtFilter] Authentication failed: Code={}, Message={}",
        e.getErrorCode().getCode(),
        e.getMessage());
    sendErrorResponse(response, e.getMessage());
  }

  private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), Map.of("error", message));
  }
}
