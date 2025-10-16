package kr.it.pullit.support.security;

import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockMemberSecurityContextFactory
    implements WithSecurityContextFactory<WithMockMember> {

  @Override
  public SecurityContext createSecurityContext(WithMockMember annotation) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    Authentication auth =
        new UsernamePasswordAuthenticationToken(annotation.memberId(), null, authorities);
    context.setAuthentication(auth);
    return context;
  }
}
