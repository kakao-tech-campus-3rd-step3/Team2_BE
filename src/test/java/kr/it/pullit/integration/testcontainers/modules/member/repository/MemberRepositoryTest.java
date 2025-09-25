package kr.it.pullit.integration.testcontainers.modules.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.support.TestContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("mock-auth")
class MemberRepositoryTest extends TestContainerTest {

  @Autowired private MemberRepository memberRepository;

  @Test
  void save_and_find_by_email() {
    // given
    Member frodo = Member.builder().email("hyeonjun@example.com").name("현준").build();
    Member sam = Member.builder().email("flareseek@example.com").name("지환").build();
    memberRepository.save(frodo);
    memberRepository.save(sam);

    // when
    Optional<Member> foundFrodo = memberRepository.findByEmail("hyeonjun@example.com");
    Optional<Member> foundSam = memberRepository.findByEmail("flareseek@example.com");

    // then
    assertThat(foundFrodo).isPresent();
    assertThat(foundSam).isPresent();
  }
}
