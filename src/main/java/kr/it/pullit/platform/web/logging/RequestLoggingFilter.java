package kr.it.pullit.platform.web.logging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(1)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
    filterChain.doFilter(wrappedRequest, response);

    logRequestDetails(wrappedRequest);
  }

  private void logRequestDetails(ContentCachingRequestWrapper request) {
    // 요청 본문이 없는 GET, DELETE 등의 메서드는 간단히 로깅
    if (!"POST".equalsIgnoreCase(request.getMethod())
        && !"PUT".equalsIgnoreCase(request.getMethod())) {
      log.info("INCOMING REQUEST: {} {}", request.getMethod(), request.getRequestURI());
      return;
    }

    StringBuilder logMessage = new StringBuilder();
    logMessage.append("\n--- INCOMING REQUEST ---\n");
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
        String bodyString = new String(content,
            request.getCharacterEncoding() != null ? request.getCharacterEncoding() : "UTF-8");
        logMessage.append(bodyString).append("\n");
      } catch (UnsupportedEncodingException e) {
        logMessage.append("Body Content: [Could not read body as text]\n");
      }
    }

    logMessage.append("--- END REQUEST ---");
    log.info(logMessage.toString());
  }
}
