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

  private final JwtTokenProvider jwtTokenPort;
  private final JwtAuthenticator jwtAuthenticator;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = jwtTokenPort.resolveToken(request);
    AuthenticationResult authResult = jwtAuthenticator.authenticate(token);
    log.debug(
        "[JwtFilter] Request URI: {}, AuthResult: {}",
        request.getRequestURI(),
        authResult.getClass().getSimpleName());

    switch (authResult) {
      case AuthenticationResult.Success(var authentication) ->
          SecurityContextHolder.getContext().setAuthentication(authentication);
      case AuthenticationResult.Expired ignored -> {
        log.warn("[JwtFilter] Expired JWT token received.");
        sendErrorResponse(response, "만료된 토큰입니다.");
        return;
      }
      case AuthenticationResult.Invalid(String errorMessage) -> {
        log.warn("[JwtFilter] Invalid JWT token received. Reason: {}", errorMessage);
        sendErrorResponse(response, "유효하지 않은 토큰입니다: " + errorMessage);
        return;
      }
      case AuthenticationResult.NoToken ignored -> {
        // 토큰이 없는 경우 아무것도 하지 않고 다음 필터로 진행
      }
    }

    filterChain.doFilter(request, response);
  }

  private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), Map.of("error", message));
  }
}
