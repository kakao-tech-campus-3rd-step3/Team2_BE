package kr.it.pullit.platform.security.config;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

public final class AuthorizationRules {

  private AuthorizationRules() {}

  public static final String[] PUBLIC_ENDPOINTS = {
    "/",
    "/api/health",
    "/login/oauth2/code/**",
    "/oauth/authorize/**",
    "/oauth2/authorization/**",
    "/auth/refresh",
    "/auth/logout",
    "/api/notifications/**"
  };

  /** 기본 인증/인가 규칙을 적용 */
  public static Customizer<
          AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>
      authenticated() {
    return authorize ->
        authorize
            .requestMatchers(PUBLIC_ENDPOINTS)
            .permitAll()
            .requestMatchers("/api/admin/**")
            .hasRole("ADMIN")
            .anyRequest()
            .authenticated();
  }
}
