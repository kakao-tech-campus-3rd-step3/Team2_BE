package kr.it.pullit.platform.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenCreationSubject;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;

public interface JwtTokenProvider {

  AuthTokens createAuthTokens(TokenCreationSubject subject);

  String createAccessToken(TokenCreationSubject subject);

  String createRefreshToken(TokenCreationSubject subject);

  TokenValidationResult validateToken(String token);

  TokenValidationResult validateRefreshToken(String token);

  String resolveToken(HttpServletRequest request);
}
