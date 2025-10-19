package kr.it.pullit.support.fixture;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.support.builder.TestMemberBuilder;

public final class MemberFixtures {

  private MemberFixtures() {}

  public static Member basicUser() {
    return TestMemberBuilder.builder().build();
  }

  public static Member userWithEmail(String email) {
    return TestMemberBuilder.builder().email(email).build();
  }

  public static Member basicAdmin() {
    return TestMemberBuilder.builder().role(Role.ADMIN).build();
  }
}
