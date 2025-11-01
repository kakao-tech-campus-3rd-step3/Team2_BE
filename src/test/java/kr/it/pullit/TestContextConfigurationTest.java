package kr.it.pullit;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.support.annotation.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"mock-auth", "test"})
@IntegrationTest
@DisplayName("통합 테스트 환경 설정 검증")
class TestContextConfigurationTest {

  @Value("${spring.application.name}")
  private String applicationName;

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @DisplayName("application.yml의 기본 속성을 성공적으로 로드한다")
  void shouldLoadPropertiesFromApplicationYml() {
    assertThat(applicationName).isEqualTo("pullit");
  }

  @Test
  @DisplayName("애플리케이션 컨텍스트가 성공적으로 로드된다")
  void contextLoads() {
    // SpringBootTest 어노테이션에 의해 컨텍스트가 로드되는 것 자체를 검증
    // 특별한 assertion이 필요 없음
    assertThat(port).isGreaterThan(0);
  }

  @Test
  @DisplayName("루트 엔드포인트에 접근할 수 있다")
  void rootEndpointIsAccessible() {
    // 간단한 API 호출을 통해 웹 서버가 정상적으로 실행되었는지 확인
    String body = this.restTemplate.getForObject("http://localhost:" + port + "/", String.class);
    // actuator의 health endpoint 등을 사용하는 것이 더 좋을 수 있음
    // 여기서는 간단히 루트 응답을 확인
    assertThat(body).isNotNull();
  }
}
