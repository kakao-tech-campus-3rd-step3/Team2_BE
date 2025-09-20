package kr.it.pullit.platform.security.config;

import kr.it.pullit.modules.auth.kakaoauth.service.CustomOAuth2UserService;
import kr.it.pullit.platform.security.handler.OAuth2AuthenticationSuccessHandler;
import kr.it.pullit.platform.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final CorsConfigurationSource corsConfigurationSource;
  private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  @Profile("!no-auth & !qa")
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/",
                        "/api",
                        "/api/health",
                        "/login/oauth2/code/**",
                        "/oauth/authorize/**",
                        "/oauth2/authorization/**",
                        "/api/auth/refresh",
                        "/api/notifications/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated());
    http.oauth2Login(
        oauth2 ->
            oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oauth2AuthenticationSuccessHandler));

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Profile("qa")
  public SecurityFilterChain qaSecurityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/",
                        "/api",
                        "/api/health",
                        "/login/oauth2/code/**",
                        "/oauth/authorize/**",
                        "/oauth2/authorization/**",
                        "/api/auth/refresh",
                        "/api/notifications/**")
                    .permitAll()
                    .anyRequest()
                    .permitAll());
    http.oauth2Login(
        oauth2 ->
            oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oauth2AuthenticationSuccessHandler));

    // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Profile("no-auth")
  public SecurityFilterChain noAuthSecurityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
    return http.build();
  }
}
