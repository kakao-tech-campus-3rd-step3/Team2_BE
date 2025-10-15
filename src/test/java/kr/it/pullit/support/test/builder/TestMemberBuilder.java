package kr.it.pullit.support.test.builder;

import kr.it.pullit.modules.member.domain.entity.Member;

public record TestMemberBuilder(Long kakaoId, String email, String nickname) {

  public static TestMemberBuilder aMember() {
    return new TestMemberBuilder(null, "tester@pullit.kr", "테스터");
  }

  public TestMemberBuilder withKakaoId(Long kakaoId) {
    return new TestMemberBuilder(kakaoId, this.email, this.nickname);
  }

  public TestMemberBuilder withEmail(String email) {
    return new TestMemberBuilder(this.kakaoId, email, this.nickname);
  }

  public TestMemberBuilder withNickname(String nickname) {
    return new TestMemberBuilder(this.kakaoId, this.email, nickname);
  }

  public Member build() {
    return Member.create(kakaoId, email, nickname);
  }
}
