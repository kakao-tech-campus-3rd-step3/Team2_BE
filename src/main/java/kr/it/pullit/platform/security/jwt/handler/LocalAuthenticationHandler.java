package kr.it.pullit.platform.security.jwt.handler;

import jakarta.servlet.http.HttpServletRequest;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.platform.security.jwt.PullitAuthenticationToken;
import kr.it.pullit.platform.security.jwt.filter.HeaderHidingRequestWrapper;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Profile("local")
@Component
public class LocalAuthenticationHandler {

  private static final Long DEFAULT_MEMBER_ID = 1L;
  private static final String DEFAULT_MEMBER_EMAIL = "dev-user@pullit.kr";

  public HttpServletRequest authenticate(final HttpServletRequest request) {
    final String authorizationHeader = request.getHeader("Authorization");

    if (shouldApplyDevAuthentication(authorizationHeader)) {
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      PullitAuthenticationToken token =
          new PullitAuthenticationToken(
              DEFAULT_MEMBER_ID, DEFAULT_MEMBER_EMAIL, null, Role.ADMIN.getAuthorities());
      context.setAuthentication(token);
      SecurityContextHolder.setContext(context);

      return new HeaderHidingRequestWrapper(request, "Authorization");
    }

    return request;
  }

  private boolean shouldApplyDevAuthentication(String authorizationHeader) {
    String token = getTokenFromHeader(authorizationHeader);
    return "1".equals(token);
  }

  private String getTokenFromHeader(String authorizationHeader) {
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    }
    return null;
  }
}
