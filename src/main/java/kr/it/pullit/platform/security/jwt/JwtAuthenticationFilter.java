package kr.it.pullit.platform.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenPort jwtTokenPort;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = jwtTokenPort.resolveToken(request);

    if (StringUtils.hasText(token)) {
      TokenValidationResult validationResult = jwtTokenPort.validateToken(token);

      if (validationResult instanceof TokenValidationResult.Valid validResult) {
        Long memberId = validResult.decodedJWT().getClaim("memberId").asLong();
        String email = validResult.decodedJWT().getClaim("email").asString();

        PullitAuthenticationToken authentication =
            new PullitAuthenticationToken(memberId, email, validResult.decodedJWT());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("[JwtFilter] Authentication successful for user: {}", email);

      } else if (validationResult instanceof TokenValidationResult.Expired) {
        log.warn("[JwtFilter] Expired JWT token received.");
        sendErrorResponse(response, "만료된 토큰입니다.");
        return;

      } else if (validationResult instanceof TokenValidationResult.Invalid invalidResult) {
        log.warn("[JwtFilter] Invalid JWT token. Reason: {}", invalidResult.cause());
        sendErrorResponse(response, "유효하지 않은 토큰입니다.");
        return;
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
