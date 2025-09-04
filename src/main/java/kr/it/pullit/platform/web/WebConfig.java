package kr.it.pullit.platform.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(WebCorsProps.class)
public class WebConfig implements WebMvcConfigurer {

  private final WebCorsProps props;

  public WebConfig(WebCorsProps props) {
    this.props = props;
  }

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    String[] origins =
        props.getAllowedOrigins().stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);

    // API 엔드포인트 - 엄격한 CORS
    registry
        .addMapping("/api/**")
        .allowedOrigins(origins)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        .allowedHeaders(
            "Content-Type", "Authorization", "X-Requested-With", "Cache-Control", "Pragma")
        .allowCredentials(true)
        .maxAge(props.getMaxAgeSeconds());

    // 루트 경로 - 엄격한 CORS
    registry
        .addMapping("/")
        .allowedOrigins(origins)
        .allowedMethods("GET", "HEAD", "OPTIONS")
        .allowedHeaders("Content-Type", "Authorization", "Cache-Control", "Pragma")
        .allowCredentials(true)
        .maxAge(props.getMaxAgeSeconds());
  }
}
