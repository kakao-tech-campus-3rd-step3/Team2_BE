package kr.it.pullit.platform.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(2)
@Slf4j
public class ResponseLoggingFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/notifications/subscribe");
  }

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
    try {
      filterChain.doFilter(request, wrappedResponse);
    } finally {
      logResponseDetails(wrappedResponse);
      wrappedResponse.copyBodyToResponse();
    }
  }

  private void logResponseDetails(ContentCachingResponseWrapper response) {
    StringBuilder logMessage = new StringBuilder();
    logMessage.append("\n🐬 --- OUTGOING RESPONSE --- 🐬\n");
    logMessage.append(String.format("Status Code : %d\n", response.getStatus()));
    logMessage.append("Headers     :\n");

    Collection<String> headerNames = response.getHeaderNames();
    for (String headerName : headerNames) {
      logMessage.append(String.format("  %s: %s\n", headerName, response.getHeader(headerName)));
    }

    logMessage.append("🐬 --- END RESPONSE --- 🐬");
    log.info(logMessage.toString());
  }
}
