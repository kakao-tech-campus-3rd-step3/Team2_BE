package kr.it.pullit.integration.testcontainers.modules.member.web;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import kr.it.pullit.support.TestContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("no-auth")
public class MemberControllerTest extends TestContainerTest {

  @Autowired private MemberRepository memberRepository;
  @Autowired private TestRestTemplate restTemplate;

  @Test
  void getMemberById() {
    // given
    Member frodo = Member.builder().email("hyeonjun@example.com").name("현준").build();
    Member sam = Member.builder().email("flareseek@example.com").name("지환").build();
    memberRepository.save(frodo);
    memberRepository.save(sam);

    ResponseEntity<MemberInfoResponse> response =
        restTemplate.getForEntity("/api/members/{id}", MemberInfoResponse.class, 1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(1L);
  }
}
