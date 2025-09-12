package kr.it.pullit.platform.security.config;

import kr.it.pullit.modules.auth.kakaoauth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;

  @Bean
  @Profile("!no-auth & !qa")
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> {})
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/",
                        "/api",
                        "/api/health",
                        "/oauth/callback/**",
                        "/login/oauth2/code/**",
                        "/oauth/authorize/**")
                    .permitAll()
                    .requestMatchers("/auth/me", "/auth/access-token/refresh", "/auth/logout")
                    .authenticated()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(
            oauth2 ->
                oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)));

    return http.build();
  }

  @Bean
  @Profile({"no-auth", "qa"})
  public SecurityFilterChain noAuthSecurityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> {})
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/auth/me", "/auth/access-token/refresh", "/auth/logout")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .oauth2Login(
            oauth2 ->
                oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)));
    return http.build();
  }
}
