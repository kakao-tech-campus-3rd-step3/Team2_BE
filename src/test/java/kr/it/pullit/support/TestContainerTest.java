package kr.it.pullit.support;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles({"testcontainers", "no-auth"})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public abstract class TestContainerTest {

  private static final String MARIA_DB_IMAGE = "mariadb:12.0.2";

  @SuppressWarnings("resource, unused")
  @Container
  @ServiceConnection
  private static final MariaDBContainer<?> MARIA_DB_CONTAINER =
      new MariaDBContainer<>(DockerImageName.parse(MARIA_DB_IMAGE))
          .withDatabaseName("pullit")
          .withUsername("test")
          .withPassword("test");
}
