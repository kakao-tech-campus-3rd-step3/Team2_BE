package kr.it.pullit.platform.web.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
@Order(1)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/notifications/subscribe") || path.startsWith("/actuator");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

    logRequestDetails(wrappedRequest);
    filterChain.doFilter(wrappedRequest, response);
  }

  private void logRequestDetails(ContentCachingRequestWrapper request) {
    StringBuilder logMessage = new StringBuilder();
    logMessage.append("\n🦁 --- INCOMING REQUEST --- 🦁\n");
    logMessage.append(String.format("URI         : %s\n", request.getRequestURI()));
    logMessage.append(String.format("Method      : %s\n", request.getMethod()));
    logMessage.append("Headers     :\n");

    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      logMessage.append(String.format("  %s: %s\n", headerName, request.getHeader(headerName)));
    }

    byte[] content = request.getContentAsByteArray();
    if (content.length > 0) {
      logMessage.append(String.format("Body Size   : %d bytes\n", content.length));
      logMessage.append("Body Content:\n");
      try {
        String bodyString =
            new String(
                content,
                request.getCharacterEncoding() != null ? request.getCharacterEncoding() : "UTF-8");
        logMessage.append(bodyString).append("\n");
      } catch (UnsupportedEncodingException e) {
        logMessage.append("Body Content: [Could not read body as text]\n");
      }
    }

    logMessage.append("🦁 --- END REQUEST --- 🦁");
    log.info(logMessage.toString());
  }
}
