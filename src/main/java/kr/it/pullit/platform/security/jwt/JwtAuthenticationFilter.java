package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenPort jwtTokenPort;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = jwtTokenPort.resolveToken(request);
    log.info("[JwtFilter] Request URI: {}", request.getRequestURI());
    log.info("[JwtFilter] Resolved Token: {}", (token != null ? "found" : "not found"));

    if (StringUtils.hasText(token)) {
      TokenValidationResult validationResult = jwtTokenPort.validateToken(token);
      log.info(
          "[JwtFilter] Token validation result: {}", validationResult.getClass().getSimpleName());

      if (validationResult instanceof TokenValidationResult.Valid(DecodedJWT decodedJwt)) {
        Long memberId = decodedJwt.getClaim("memberId").asLong();
        String email = decodedJwt.getClaim("email").asString();
        PullitAuthenticationToken authentication =
            new PullitAuthenticationToken(memberId, email, decodedJwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info(
            "[JwtFilter] Authentication successful, set in SecurityContext for user: {}", email);
        log.info(
            "[JwtFilter] Verification check in filter: {}",
            SecurityContextHolder.getContext().getAuthentication());

      } else if (validationResult instanceof TokenValidationResult.Expired) {
        log.warn("[JwtFilter] Expired JWT token received.");
        sendErrorResponse(response, "만료된 토큰");
        return;
      } else if (validationResult instanceof TokenValidationResult.Invalid(String errorMessage)) {
        log.warn("[JwtFilter] Invalid JWT token received. Reason: {}", errorMessage);
        sendErrorResponse(response, "유효하지 않은 토큰: " + errorMessage);
        return;
      }
    } else {
      log.info("[JwtFilter] No JWT token found in Authorization header.");
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
