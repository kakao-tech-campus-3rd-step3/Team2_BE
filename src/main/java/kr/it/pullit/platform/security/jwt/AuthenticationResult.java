package kr.it.pullit.platform.security.jwt;

public sealed interface AuthenticationResult {
  record Success(PullitAuthenticationToken authentication) implements AuthenticationResult {}

  record Expired() implements AuthenticationResult {}

  record Invalid(String message) implements AuthenticationResult {}

  record NoToken() implements AuthenticationResult {}
}
