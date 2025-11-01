package kr.it.pullit.platform.web;

import java.time.Duration;
import java.util.List;
import kr.it.pullit.platform.web.resolver.NotNullAuthenticationPrincipalArgumentResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(WebCorsProps.class)
public class WebConfig implements WebMvcConfigurer {

  private final WebCorsProps props;
  private final NotNullAuthenticationPrincipalArgumentResolver
      notNullAuthenticationPrincipalArgumentResolver;

  public WebConfig(
      WebCorsProps props,
      NotNullAuthenticationPrincipalArgumentResolver
          notNullAuthenticationPrincipalArgumentResolver) {
    this.props = props;
    this.notNullAuthenticationPrincipalArgumentResolver =
        notNullAuthenticationPrincipalArgumentResolver;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOriginPatterns(props.getAllowedOrigins());
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(Duration.ofSeconds(props.getMaxAgeSeconds()));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {}

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(notNullAuthenticationPrincipalArgumentResolver);
  }
}
