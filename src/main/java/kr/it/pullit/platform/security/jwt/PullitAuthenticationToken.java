package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Collection;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class PullitAuthenticationToken extends AbstractAuthenticationToken {

  private final Long memberId;
  @Getter private final String email;
  @Getter private final DecodedJWT decodedJwt;

  public PullitAuthenticationToken(
      Long memberId,
      String email,
      DecodedJWT decodedJwt,
      Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.memberId = memberId;
    this.email = email;
    this.decodedJwt = decodedJwt;
    setAuthenticated(true);
  }

  public PullitAuthenticationToken(Long memberId, String email, DecodedJWT decodedJwt) {
    this(memberId, email, decodedJwt, null);
  }

  @Override
  public Object getCredentials() {
    return decodedJwt.getToken();
  }

  @Override
  public Long getPrincipal() {
    return memberId;
  }
}
