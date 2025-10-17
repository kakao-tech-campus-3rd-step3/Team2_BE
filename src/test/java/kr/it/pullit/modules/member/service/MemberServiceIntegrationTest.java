package kr.it.pullit.modules.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.support.annotation.IntegrationTest;
import kr.it.pullit.support.fixture.MemberFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DisplayName("MemberService 통합 테스트")
class MemberServiceIntegrationTest {

  @Autowired private MemberService memberService;

  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("사용자에게 어드민 권한을 부여하면, DB의 role이 ADMIN으로 변경된다")
  void shouldGrantAdminRole() {
    // given
    Member user = memberRepository.save(MemberFixtures.basicUser());
    assertThat(user.getRole()).isEqualTo(Role.MEMBER);

    // when
    memberService.grantAdminRole(user.getId());

    // then
    Member admin = memberRepository.findById(user.getId()).get();
    assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  @DisplayName("어드민 사용자의 권한을 회수하면, DB의 role이 USER로 변경된다")
  void shouldRevokeAdminRole() {
    // given
    Member admin = memberRepository.save(MemberFixtures.basicAdmin());
    assertThat(admin.getRole()).isEqualTo(Role.ADMIN);

    // when
    memberService.revokeAdminRole(admin.getId());

    // then
    Member user = memberRepository.findById(admin.getId()).get();
    assertThat(user.getRole()).isEqualTo(Role.MEMBER);
  }
}
