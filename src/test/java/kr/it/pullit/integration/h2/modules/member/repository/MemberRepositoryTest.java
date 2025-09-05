package kr.it.pullit.integration.h2.modules.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.support.H2Test;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
class MemberRepositoryTest extends H2Test {

  @Autowired private MemberRepository memberRepository;

  @Test
  void findByEmail() {
    // given
    String givenEmail = "test@example.com";
    Member newMember = Member.builder().email(givenEmail).name("test").build();
    Member save = memberRepository.save(newMember);
    log.info("save member: {}", save);
    // when
    var result = memberRepository.findByEmail(givenEmail);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo(givenEmail);
  }
}
