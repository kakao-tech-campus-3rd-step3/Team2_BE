package kr.it.pullit.support.config;

import static kr.it.pullit.platform.security.config.AuthorizationRules.authenticated;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * MVC 슬라이스 테스트를 위한 간단한 Security 설정. 실제 SecurityConfig의 의존성을 모두 가져오지 않기 위해 사용합니다.
 */
@TestConfiguration
public class TestSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(authenticated());
    return http.build();
  }
}
