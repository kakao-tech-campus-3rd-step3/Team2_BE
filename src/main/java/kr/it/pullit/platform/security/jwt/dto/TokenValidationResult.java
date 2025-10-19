package kr.it.pullit.platform.security.jwt.dto;

import com.auth0.jwt.interfaces.DecodedJWT;

public sealed interface TokenValidationResult {
  default boolean isValid() {
    return this instanceof Valid;
  }

  record Valid(DecodedJWT decodedJwt) implements TokenValidationResult {}

  record Expired() implements TokenValidationResult {}

  record Invalid(String errorMessage) implements TokenValidationResult {}
}
