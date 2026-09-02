package kr.it.pullit.platform.security.config;

import java.util.Optional;
import kr.it.pullit.modules.auth.kakaoauth.service.CustomOAuth2UserService;
import kr.it.pullit.platform.security.handler.OAuth2AuthenticationSuccessHandler;
import kr.it.pullit.platform.security.jwt.exception.JwtAuthenticationEntryPoint;
import kr.it.pullit.platform.security.jwt.filter.DevAuthenticationFilter;
import kr.it.pullit.platform.security.jwt.filter.JwtAuthenticationFilter;
import kr.it.pullit.platform.security.repository.OAuth2AuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;

/** 활성화된 Spring 프로필에 따라 다른 보안 필터 체인(SecurityFilterChain)을 구성하여 인증/인가 정책을 환경별로 다르게 적용. */
@Profile("!worker")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Value("${app.oauth2.authorized-redirect-uri}")
  private String authorizedRedirectUri;

  private final CustomOAuth2UserService customOAuth2UserService;
  private final CorsConfigurationSource corsConfigurationSource;
  private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final OAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
  private final Optional<DevAuthenticationFilter> devAuthenticationFilter;

  @Bean
  @Order(0)
  public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/actuator/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/health", "/actuator/prometheus")
                    .permitAll()
                    .anyRequest()
                    .denyAll())
        .requestCache(rc -> rc.disable()) // Saved request 방지
        .exceptionHandling(ex -> ex.disable()) // 불필요한 EntryPoint/Redirect 제거
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  private AuthenticationFailureHandler oauth2FailureHandler() {
    return (request, response, ex) -> {
      httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);

      String targetUrl =
          UriComponentsBuilder.fromUriString(authorizedRedirectUri)
              .queryParam("error", "oauth2_login_failed")
              .build()
              .toUriString();

      response.sendRedirect(targetUrl);
    };
  }

  private void applyCommon(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
  }

  private void configureOAuth2Login(HttpSecurity http) throws Exception {
    http.oauth2Login(
        oauth2 ->
            oauth2
                .authorizationEndpoint(
                    config ->
                        config.authorizationRequestRepository(
                            httpCookieOAuth2AuthorizationRequestRepository))
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oauth2AuthenticationSuccessHandler)
                .failureHandler(oauth2FailureHandler()));
  }

  @Bean
  @Order(1)
  @Profile("!local")
  public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**");
    applyCommon(http);

    http.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

    http.authorizeHttpRequests(AuthorizationRules.authenticated());
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Order(2)
  @Profile("!local")
  public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/**");
    applyCommon(http);
    configureOAuth2Login(http); // OAuth2는 웹 체인에만 적용

    http.exceptionHandling(
        ex ->
            ex.authenticationEntryPoint(
                new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/kakao")));

    http.authorizeHttpRequests(AuthorizationRules.authenticated());

    return http.build();
  }

  @Bean
  @Order(1)
  @Profile("local")
  public SecurityFilterChain apiChainForLocal(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**");
    applyCommon(http);

    http.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    devAuthenticationFilter.ifPresent(
        filter -> http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class));
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Order(2)
  @Profile("local")
  public SecurityFilterChain webChainForLocal(HttpSecurity http) throws Exception {
    http.securityMatcher("/**");
    applyCommon(http);
    configureOAuth2Login(http);

    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

    return http.build();
  }
}
