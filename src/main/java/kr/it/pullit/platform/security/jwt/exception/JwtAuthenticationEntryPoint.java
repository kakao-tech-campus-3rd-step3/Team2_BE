package kr.it.pullit.platform.security.jwt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import kr.it.pullit.shared.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    ErrorCode errorCode;

    if (authException instanceof JwtAuthenticationException jwtAuthException) {
      errorCode = jwtAuthException.getErrorCode();
    } else {
      errorCode = TokenErrorCode.AUTHENTICATION_FAILED;
    }

    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(errorCode.getStatus(), errorCode.getMessage());
    problemDetail.setProperty("code", errorCode.getCode());
    problemDetail.setInstance(URI.create(request.getRequestURI()));

    objectMapper.writeValue(response.getWriter(), problemDetail);
  }
}
