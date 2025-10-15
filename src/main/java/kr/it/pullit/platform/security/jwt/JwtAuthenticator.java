package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticator {

  private final JwtTokenPort jwtTokenPort;

  public AuthenticationResult authenticate(String token) {
    if (!StringUtils.hasText(token)) {
      return new AuthenticationResult.NoToken();
    }

    TokenValidationResult validationResult = jwtTokenPort.validateToken(token);

    return switch (validationResult) {
      case TokenValidationResult.Valid(DecodedJWT decodedJwt) -> {
        Long memberId = decodedJwt.getClaim("memberId").asLong();
        String email = decodedJwt.getClaim("email").asString();
        var authentication = new PullitAuthenticationToken(memberId, email, decodedJwt);
        yield new AuthenticationResult.Success(authentication);
      }
      case TokenValidationResult.Expired ignored -> new AuthenticationResult.Expired();
      case TokenValidationResult.Invalid(String message) ->
          new AuthenticationResult.Invalid(message);
    };
  }
}
