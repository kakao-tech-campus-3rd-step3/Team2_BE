package kr.it.pullit.platform.security.config;

import java.util.Optional;
import kr.it.pullit.modules.auth.kakaoauth.service.CustomOAuth2UserService;
import kr.it.pullit.platform.security.handler.OAuth2AuthenticationSuccessHandler;
import kr.it.pullit.platform.security.jwt.filter.DevAuthenticationFilter;
import kr.it.pullit.platform.security.jwt.filter.JwtAuthenticationFilter;
import kr.it.pullit.platform.security.repository.OAuth2AuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * 애플리케이션의 Spring Security 설정을 담당합니다.
 *
 * <p>활성화된 Spring 프로필(@Profile)에 따라 서로 다른 보안 필터 체인(SecurityFilterChain)을 구성하여, 인증/인가 정책을 환경별로 다르게
 * 적용합니다.
 *
 * <ul>
 *   <li><b>auth:</b> 인증이 필요한 운영 환경용 보안 설정을 적용합니다.
 *   <li><b>qa:</b> 인증을 비활성화하여 테스트 편의성을 높인 QA 환경용 설정을 적용합니다.
 *   <li><b>default (지정 없음):</b> 'auth' 또는 'qa' 프로필이 아닐 때 적용되는 기본 설정으로, 모든 요청을 허용합니다. (예: 로컬 개발 환경)
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final CorsConfigurationSource corsConfigurationSource;
  private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
  private final Optional<DevAuthenticationFilter> devAuthenticationFilter;

  private static final AuthenticationFailureHandler OAUTH2_FAILURE_HANDLER =
      (request, response, ex) -> {
        LoggerFactory.getLogger("OAuth2Failure")
            .error(
                "[OAUTH2_FAILURE] errorClass={}, message={}, state={}, redirectUriFromSession={}",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getParameter("state"),
                request.getSession(false) == null
                    ? null
                    : request
                        .getSession(false)
                        .getAttribute(OAuth2AuthenticationSuccessHandler.REDIRECT_URI_SESSION_KEY));
        response.sendRedirect("/login?error");
      };

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
                .failureHandler(OAUTH2_FAILURE_HANDLER));
  }

  @Bean
  @Order(1)
  @Profile("!local")
  public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/**");
    applyCommon(http);

    http.exceptionHandling(
        ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

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
  @Profile("local")
  public SecurityFilterChain localChain(HttpSecurity http) throws Exception {
    applyCommon(http);
    http.authorizeHttpRequests(
        authorize ->
            authorize
                .requestMatchers(AuthorizationRules.PUBLIC_ENDPOINTS)
                .permitAll()
                .anyRequest()
                .permitAll());
    devAuthenticationFilter.ifPresent(
        filter -> http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class));
    return http.build();
  }
}
