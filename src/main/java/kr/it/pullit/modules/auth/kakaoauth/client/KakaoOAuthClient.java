package kr.it.pullit.modules.auth.kakaoauth.client;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KakaoOAuthClient {

  private static final int CONNECTION_TIME_OUT_SECOND = 10; // TODO: 정확한 시간 정책.
  private static final int READ_TIME_OUT_SECOND = 10;

  @Bean(name = "kakaoRestClient")
  public RestClient kakaoRestClient(RestTemplateBuilder builder) {
    RestTemplate restTemplate =
        builder
            .connectTimeout(Duration.ofSeconds(CONNECTION_TIME_OUT_SECOND))
            .readTimeout(Duration.ofSeconds(READ_TIME_OUT_SECOND))
            .build();
    return RestClient.create(restTemplate);
  }
}
