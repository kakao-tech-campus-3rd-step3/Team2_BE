package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class PullitAuthenticationToken extends AbstractAuthenticationToken {

  private final Long memberId;
  private final String email;
  private final DecodedJWT decodedJwt;

  public PullitAuthenticationToken(Long memberId, String email, DecodedJWT decodedJwt) {
    super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    this.memberId = memberId;
    this.email = email;
    this.decodedJwt = decodedJwt;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return decodedJwt.getToken();
  }

  @Override
  public Object getPrincipal() {
    return memberId;
  }

  public String getEmail() {
    return email;
  }
}
