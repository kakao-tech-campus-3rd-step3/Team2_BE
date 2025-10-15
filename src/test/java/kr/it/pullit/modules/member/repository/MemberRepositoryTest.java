package kr.it.pullit.modules.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.support.annotation.JpaSliceTest;
import kr.it.pullit.support.builder.TestMemberBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@JpaSliceTest
@Import(MemberRepositoryImpl.class)
@DisplayName("MemberRepository 슬라이스 테스트")
class MemberRepositoryTest {

  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("회원을 저장하고 이메일로 조회하면, 저장된 회원이 조회되어야 한다")
  void shouldSaveAndFindMemberByEmail() {
    // given
    Member frodo =
        TestMemberBuilder.builder().withEmail("hyeonjun@example.com").withNickname("현준").build();
    Member sam =
        TestMemberBuilder.builder().withEmail("flareseek@example.com").withNickname("지환").build();
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
