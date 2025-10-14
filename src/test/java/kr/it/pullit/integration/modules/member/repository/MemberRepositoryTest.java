package kr.it.pullit.integration.modules.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("real-env")
@IntegrationTest
class MemberRepositoryTest {

  @Autowired private MemberRepository memberRepository;

  @Test
  void save_and_find_by_email() {
    // given
    Member frodo = Member.create(null, "hyeonjun@example.com", "현준");
    Member sam = Member.create(null, "flareseek@example.com", "지환");
    memberRepository.save(frodo);
    memberRepository.save(sam);

    // when
    Optional<Member> foundFrodo = memberRepository.findByEmail("hyeonjun@example.com");
    Optional<Member> foundSam = memberRepository.findByEmail("flareseek@example.com");

    // then
    assertThat(foundFrodo).isPresent();
    assertThat(foundSam).isPresent();
    assertThat(foundFrodo.get().getName()).isEqualTo("현준");
    assertThat(foundSam.get().getName()).isEqualTo("지환");
  }
}
