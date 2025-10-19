package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenCreationSubject;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String TOKEN_TYPE_CLAIM = "tokenType";
  private static final String TOKEN_TYPE_ACCESS = "access";
  private static final String TOKEN_TYPE_REFRESH = "refresh";
  private final JwtProps jwtProps;
  private final Algorithm algorithm;
  private final JWTVerifier verifier;

  public JwtTokenProviderImpl(JwtProps jwtProps) {
    this.jwtProps = jwtProps;
    this.algorithm = Algorithm.HMAC256(jwtProps.secret());
    this.verifier =
        JWT.require(algorithm)
            .withIssuer(jwtProps.issuer())
            .withAudience(jwtProps.audience())
            .build();
  }

  @Override
  public AuthTokens createAuthTokens(TokenCreationSubject subject) {
    return new AuthTokens(createAccessToken(subject), createRefreshToken(subject));
  }

  @Override
  public String createAccessToken(TokenCreationSubject subject) {
    Instant now = Instant.now();
    Instant expiration = now.plus(jwtProps.accessTokenExpirationMinutes());

    return createToken(subject, now, expiration, TOKEN_TYPE_ACCESS);
  }

  @Override
  public String createRefreshToken(TokenCreationSubject subject) {
    Instant now = Instant.now();
    Instant expiration = now.plus(jwtProps.refreshTokenExpirationDays());

    return createToken(subject, now, expiration, TOKEN_TYPE_REFRESH);
  }

  private String createToken(
      TokenCreationSubject subject, Instant now, Instant expiration, String tokenType) {
    return JWT.create()
        .withSubject(subject.email())
        .withClaim("memberId", subject.memberId())
        .withClaim("email", subject.email())
        .withClaim("role", subject.role().name())
        .withClaim(TOKEN_TYPE_CLAIM, tokenType)
        .withIssuer(jwtProps.issuer())
        .withAudience(jwtProps.audience())
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(expiration))
        .sign(algorithm);
  }

  @Override
  public TokenValidationResult validateToken(String token) {
    return validateTokenInternal(token, TOKEN_TYPE_ACCESS, "액세스 토큰이 아닙니다.");
  }

  @Override
  public TokenValidationResult validateRefreshToken(String token) {
    return validateTokenInternal(token, TOKEN_TYPE_REFRESH, "리프레시 토큰이 아닙니다.");
  }

  private TokenValidationResult validateTokenInternal(
      String token, String expectedTokenType, String invalidTypeMessage) {
    if (!StringUtils.hasText(token)) {
      return new TokenValidationResult.Invalid("빈 토큰입니다.");
    }
    try {
      DecodedJWT decodedJwt = verifier.verify(token);
      return validateTokenType(decodedJwt, expectedTokenType, invalidTypeMessage);
    } catch (TokenExpiredException e) {
      return new TokenValidationResult.Expired();
    } catch (JWTVerificationException e) {
      return new TokenValidationResult.Invalid("토큰 검증 실패: " + e.getMessage());
    } catch (Exception e) {
      return new TokenValidationResult.Invalid("예상치 못한 오류: " + e.getMessage());
    }
  }

  private TokenValidationResult validateTokenType(
      DecodedJWT decodedJwt, String expectedTokenType, String invalidTypeMessage) {
    String tokenType = decodedJwt.getClaim(TOKEN_TYPE_CLAIM).asString();
    if (!expectedTokenType.equals(tokenType)) {
      return new TokenValidationResult.Invalid(invalidTypeMessage);
    }
    return new TokenValidationResult.Valid(decodedJwt);
  }

  @Override
  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
