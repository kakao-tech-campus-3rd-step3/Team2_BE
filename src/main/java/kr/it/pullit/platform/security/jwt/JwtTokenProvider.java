package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.platform.security.config.JwtProps;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements JwtTokenPort {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private final JwtProps jwtProps;
  private final Algorithm algorithm;
  private final JWTVerifier verifier;

  public JwtTokenProvider(JwtProps jwtProps) {
    this.jwtProps = jwtProps;
    this.algorithm = Algorithm.HMAC256(jwtProps.secret());
    this.verifier =
        JWT.require(algorithm)
            .withIssuer(jwtProps.issuer())
            .withAudience(jwtProps.audience())
            .build();
  }

  @Override
  public AuthTokens createAuthTokens(Long memberId, String email, Role role) {
    return new AuthTokens(
        createAccessToken(memberId, email, role), createRefreshToken(memberId, email, role));
  }

  @Override
  public String createAccessToken(Long memberId, String email, Role role) {
    Instant now = Instant.now();
    Instant expiration = now.plus(jwtProps.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);

    return JWT.create()
        .withSubject(email)
        .withClaim("memberId", memberId)
        .withClaim("email", email)
        .withClaim("role", role.name())
        .withClaim("tokenType", "access")
        .withIssuer(jwtProps.issuer())
        .withAudience(jwtProps.audience())
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(expiration))
        .sign(algorithm);
  }

  @Override
  public String createRefreshToken(Long memberId, String email, Role role) {
    Instant now = Instant.now();
    Instant expiration = now.plus(jwtProps.refreshTokenExpirationDays(), ChronoUnit.DAYS);

    return JWT.create()
        .withSubject(email)
        .withClaim("memberId", memberId)
        .withClaim("email", email)
        .withClaim("role", role.name())
        .withClaim("tokenType", "refresh")
        .withIssuer(jwtProps.issuer())
        .withAudience(jwtProps.audience())
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(expiration))
        .sign(algorithm);
  }

  @Override
  public TokenValidationResult validateToken(String token) {
    try {
      DecodedJWT decodedJwt = verifier.verify(token);
      return new TokenValidationResult.Valid(decodedJwt);
    } catch (TokenExpiredException e) {
      return new TokenValidationResult.Expired();
    } catch (JWTVerificationException e) {
      return new TokenValidationResult.Invalid("토큰 검증 실패: " + e.getMessage());
    } catch (Exception e) {
      return new TokenValidationResult.Invalid("예상치 못한 오류: " + e.getMessage());
    }
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
