package kr.it.pullit.platform.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import kr.it.pullit.modules.auth.kakaoauth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final CorsConfigurationSource corsConfigurationSource;

  @Bean
  @Profile("!no-auth & !qa")
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/", "/api", "/api/health", "/login/oauth2/code/**",
                "/oauth/authorize/**", "/oauth2/authorization/**", "/api/auth/refresh")
            .permitAll().anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)));

    return http.build();
  }

  @Bean
  @Profile("qa")
  public SecurityFilterChain qaSecurityFilterChain(HttpSecurity http) throws Exception {
    // TODO: QA 환경의 모든 요청을 임시로 허용하고 있습니다. 추후 인증 로직을 추가
    http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        // .sessionManagement(
        // session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // .authorizeHttpRequests(authorize -> authorize.requestMatchers("/", "/api", "/api/health",
        // "/api/notifications/**", "/login/oauth2/code/**", "/oauth/authorize/**",
        // "/oauth2/authorization/**", "/api/auth/refresh").permitAll().anyRequest()
        // .authenticated())
        // .oauth2Login(oauth2 -> oauth2
        // .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)));
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
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
