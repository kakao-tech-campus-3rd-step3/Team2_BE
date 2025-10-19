package kr.it.pullit.platform.security.jwt.dto;

import com.auth0.jwt.interfaces.DecodedJWT;

public sealed interface TokenValidationResult {
  boolean isValid();

  record Valid(DecodedJWT decodedJwt) implements TokenValidationResult {
    @Override
    public boolean isValid() {
      return true;
    }
  }

  record Expired() implements TokenValidationResult {
    @Override
    public boolean isValid() {
      return false;
    }
  }

  record Invalid(String errorMessage) implements TokenValidationResult {
    @Override
    public boolean isValid() {
      return false;
    }
  }
}
