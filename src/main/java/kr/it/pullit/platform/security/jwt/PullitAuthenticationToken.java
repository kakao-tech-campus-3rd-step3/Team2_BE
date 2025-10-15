package kr.it.pullit.platform.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;

public class PullitAuthenticationToken extends AbstractAuthenticationToken {

  private final Long memberId;
  @Getter
  private final String email;
  @Getter
  private final DecodedJWT decodedJwt;

  public PullitAuthenticationToken(Long memberId, String email, DecodedJWT decodedJwt) {
    super(null);
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
  public Long getPrincipal() {
    return memberId;
  }
}
