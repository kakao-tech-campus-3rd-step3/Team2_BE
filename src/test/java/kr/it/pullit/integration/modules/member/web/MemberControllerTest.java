package kr.it.pullit.integration.modules.member.web;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import kr.it.pullit.support.IntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"mock-auth", "real-env"})
@IntegrationTest
public class MemberControllerTest {

  @LocalServerPort private int port;
  @Autowired private MemberRepository memberRepository;
  @Autowired private TestRestTemplate restTemplate;

  // TODO: later fix.
  @Disabled("later fix")
  @Test
  void getMemberById() {
    // given
    Member frodo = Member.create(1L, "hyeonjun@example.com", "현준");
    Member sam = Member.create(2L, "flareseek@example.com", "지환");
    memberRepository.save(frodo);
    memberRepository.save(sam);

    ResponseEntity<MemberInfoResponse> response =
        restTemplate.getForEntity("/api/members/{id}", MemberInfoResponse.class, 1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(1L);
  }
}
