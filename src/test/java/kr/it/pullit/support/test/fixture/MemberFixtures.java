package kr.it.pullit.support.test.fixture;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.support.test.builder.TestMemberBuilder;

public final class MemberFixtures {

  private MemberFixtures() {}

  public static Member basic() {
    return TestMemberBuilder.builder().build();
  }

  public static Member withEmail(String email) {
    return TestMemberBuilder.builder().withEmail(email).build();
  }
}
