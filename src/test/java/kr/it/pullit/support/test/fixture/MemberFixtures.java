package kr.it.pullit.support.test.fixture;

import static kr.it.pullit.support.test.builder.TestMemberBuilder.aMember;
import kr.it.pullit.modules.member.domain.entity.Member;

public final class MemberFixtures {

  private MemberFixtures() {}

  public static Member basic() {
    return aMember().build();
  }

  public static Member withEmail(String email) {
    return aMember().withEmail(email).build();
  }
}
