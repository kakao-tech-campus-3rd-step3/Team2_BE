package kr.it.pullit.integration.testcontainers.modules.member.web;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.support.TestContainerTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MemberControllerTest extends TestContainerTest {

  @Autowired private TestRestTemplate restTemplate;

  //TODO: later fix.
  @Disabled("later fix")
  @Test
  void getMemberById() {
    ResponseEntity<String> response =
        restTemplate.getForEntity("/api/members/{id}", String.class, 1L);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
