package kr.it.pullit.support;

import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;

@Disabled
@ActiveProfiles("testcontainers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class TestcontainersIntegrationTest {

  @TestConfiguration(proxyBeanMethods = false)
  static class TestContainersConfig {
    @Bean
    @ServiceConnection
    public MariaDBContainer<?> mariadbContainer(
        @Value("${testcontainers.mariadb.image}") String imageName) {
      return new MariaDBContainer<>(imageName);
    }
  }
}
