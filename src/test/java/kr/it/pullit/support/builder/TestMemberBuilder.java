package kr.it.pullit.support.builder;

import java.util.concurrent.atomic.AtomicLong;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import lombok.Builder;

public record TestMemberBuilder() {

  private static final AtomicLong kakaoIdSequence = new AtomicLong(System.currentTimeMillis());

  // Lombok 빌더가 Member 객체를 직접 생성하도록 static 메서드에 @Builder를 적용합니다.
  @Builder(builderMethodName = "internalBuilder")
  private static Member build(Long kakaoId, String email, String name, Role role) {
    if (Role.ADMIN.equals(role)) {
      return Member.createAdmin(kakaoId, email, name);
    }
    return Member.createMember(kakaoId, email, name);
  }

  // 외부에서 사용할 빌더 진입점을 제공하며, 테스트에 필요한 기본값들을 설정합니다.
  public static MemberBuilder builder() {
    String uniqueEmail = "tester" + System.nanoTime() + "@pullit.kr";

    return internalBuilder()
        .kakaoId(kakaoIdSequence.incrementAndGet())
        .email(uniqueEmail)
        .name("테스터")
        .role(Role.MEMBER);
  }
}
