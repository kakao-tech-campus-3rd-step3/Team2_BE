package kr.it.pullit.support.fixture;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.support.builder.TestMemberBuilder;

public final class MemberFixtures {

  private MemberFixtures() {}

  public static Member basic() {
    return TestMemberBuilder.builder().build();
  }

  public static Member withEmail(String email) {
    return TestMemberBuilder.builder().withEmail(email).build();
  }
}
