package kr.it.pullit.integration.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.support.TestContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("Testcontainers 통합 테스트 예시")
class SampleTest extends TestContainerTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("MariaDB 컨테이너가 성공적으로 실행되고 연결된다")
  void container_is_running_and_connected() {
    // given
    String query = "SELECT 1";

    // when
    Integer result = jdbcTemplate.queryForObject(query, Integer.class);

    // then
    assertThat(result).isEqualTo(1);
  }
}
