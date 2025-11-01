package kr.it.pullit.modules.member.domain.entity;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
@RequiredArgsConstructor
public enum Role {
  MEMBER("ROLE_MEMBER"),
  ADMIN("ROLE_ADMIN");

  private final String key;

  public List<SimpleGrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(this.key));
  }
}
