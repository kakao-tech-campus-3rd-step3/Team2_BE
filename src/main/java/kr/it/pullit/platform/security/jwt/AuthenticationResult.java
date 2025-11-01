package kr.it.pullit.platform.security.jwt;

import java.util.Optional;

public sealed interface AuthenticationResult {

  record Success(PullitAuthenticationToken authentication) implements AuthenticationResult {
    @Override
    public Optional<PullitAuthenticationToken> getAuthentication() {
      return Optional.of(authentication);
    }
  }

  record NoToken() implements AuthenticationResult {}

  default Optional<PullitAuthenticationToken> getAuthentication() {
    return Optional.empty();
  }
}
