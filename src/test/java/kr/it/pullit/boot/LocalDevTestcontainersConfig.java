package kr.it.pullit.boot;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class LocalDevTestcontainersConfig {

  @Bean
  @ServiceConnection
  @RestartScope
  @SuppressWarnings("resource")
  public MariaDBContainer<?> mariadbContainer() {
    return new MariaDBContainer<>(DockerImageName.parse("mariadb:12.0.2"))
        .withDatabaseName("pullit")
        .withUsername("test")
        .withPassword("test");
  }
}
