package kr.it.pullit.support.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import kr.it.pullit.modules.member.domain.entity.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMemberSecurityContextFactory.class)
public @interface WithMockMember {
  long memberId() default 1L;

  Role role() default Role.MEMBER;
}
