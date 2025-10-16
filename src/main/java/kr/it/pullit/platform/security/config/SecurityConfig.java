package kr.it.pullit.platform.security.config;

import kr.it.pullit.modules.auth.kakaoauth.service.CustomOAuth2UserService;
import kr.it.pullit.platform.security.handler.OAuth2AuthenticationSuccessHandler;
import kr.it.pullit.platform.security.jwt.JwtAuthenticationFilter;
import kr.it.pullit.platform.security.repository.OAuth2AuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
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
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final CorsConfigurationSource corsConfigurationSource;
  private final OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final OAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

  private static final String[] PUBLIC_ENDPOINTS = {
    "/",
    "/api/health",
    "/login/oauth2/code/**",
    "/oauth/authorize/**",
    "/oauth2/authorization/**",
    "/auth/refresh",
    "/auth/logout",
    "/api/notifications/**"
  };

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
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.exceptionHandling(
        handler ->
            handler.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                PathPatternRequestMatcher.withDefaults().matcher("/api/**")));
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

  /**
   * 'auth' 프로필 활성화 시 적용되는 보안 필터 체인입니다.
   *
   * <p>인증이 필요한 운영 환경을 대상으로 하며, 특정 경로를 제외한 모든 요청에 대해 인증을 요구합니다. JWT 토큰 기반의 인증 필터가 활성화됩니다.
   *
   * @param http HttpSecurity 설정 객체
   * @return 구성된 SecurityFilterChain
   * @throws Exception 설정 과정에서 발생할 수 있는 예외
   */
  @Bean
  @Profile("auth")
  public SecurityFilterChain authSecurityFilterChain(HttpSecurity http) throws Exception {
    applyCommon(http);
    http.authorizeHttpRequests(
        authorize ->
            authorize.requestMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated());
    configureOAuth2Login(http);

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * 'qa' 프로필 활성화 시 적용되는 보안 필터 체인입니다.
   *
   * <p>QA(품질 보증) 환경에서의 테스트 편의성을 위해, 특정 경로 외 모든 요청을 허용(permitAll)합니다. JWT 필터는 비활성화 상태입니다.
   *
   * @param http HttpSecurity 설정 객체
   * @return 구성된 SecurityFilterChain
   * @throws Exception 설정 과정에서 발생할 수 있는 예외
   */
  @Bean
  @Profile("qa")
  public SecurityFilterChain qaSecurityFilterChain(HttpSecurity http) throws Exception {
    applyCommon(http);
    http.authorizeHttpRequests(
        authorize ->
            authorize.requestMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().permitAll());
    configureOAuth2Login(http);

    // http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * 'auth' 또는 'qa' 프로필이 활성화되지 않았을 때 적용되는 기본 보안 필터 체인입니다.
   *
   * <p>주로 로컬 개발 환경에서 사용되며, 모든 요청을 허용하여 개발 및 테스트의 편의성을 극대화합니다.
   *
   * @param http HttpSecurity 설정 객체
   * @return 구성된 SecurityFilterChain
   * @throws Exception 설정 과정에서 발생할 수 있는 예외
   */
  @Bean
  @Profile("!auth & !qa")
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    applyCommon(http);
    http.authorizeHttpRequests(
        authorize ->
            authorize.requestMatchers(PUBLIC_ENDPOINTS).permitAll().anyRequest().permitAll());
    configureOAuth2Login(http);
    return http.build();
  }
}
