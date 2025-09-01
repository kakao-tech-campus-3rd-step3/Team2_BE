package kr.it.pullit.platform.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Profile("!no-auth")
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/health", "/oauth/callback/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/auth/me", "/auth/access-token/refresh", "/auth/logout").authenticated()
                        .anyRequest().authenticated()
                );
                // .oauth2Login(withDefaults()); TODO : 소셜 로그인 기능 완료되면 이 부분 주석 해제

        return http.build();
    }

    @Bean
    @Profile("no-auth")
    public SecurityFilterChain noAuthSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/health", "/oauth/callback/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers("/auth/me", "/auth/access-token/refresh", "/auth/logout").authenticated()
                        .anyRequest().permitAll()
                );
                // .oauth2Login(withDefaults()); TODO : 소셜 로그인 기능 완료되면 이 부분 주석 해제

        return http.build();
    }
}

