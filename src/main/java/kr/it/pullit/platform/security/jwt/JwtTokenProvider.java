package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
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
  public AuthTokens createAuthTokens(Member member) {
    return new AuthTokens(createAccessToken(member), createRefreshToken(member));
  }

  @Override
  public String createAccessToken(Member member) {
    Instant now = Instant.now();
    Instant expiration = now.plus(jwtProps.accessTokenExpirationMinutes());

    return JWT.create()
        .withSubject(String.valueOf(member.getId()))
        .withClaim("email", member.getEmail())
        .withIssuer(jwtProps.issuer())
        .withAudience(jwtProps.audience())
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(expiration))
        .sign(algorithm);
  }

  @Override
  public String createRefreshToken(Member member) {
    Instant now = Instant.now();
    Instant expiration = now.plus(jwtProps.refreshTokenExpirationDays());

    return JWT.create()
        .withSubject(String.valueOf(member.getId()))
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
      log.info("Expired JWT token: {}", e.getMessage());
      return new TokenValidationResult.Expired();
    } catch (JWTVerificationException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
      return new TokenValidationResult.Invalid(e.getMessage());
    }
  }

  @Override
  public Authentication getAuthentication(String token) {
    try {
      DecodedJWT decodedJWT = verifier.verify(token);
      Long memberId = Long.valueOf(decodedJWT.getSubject());
      return new UsernamePasswordAuthenticationToken(
          memberId, "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    } catch (JWTVerificationException e) {
      log.warn("Cannot get Authentication from token: {}", e.getMessage());
      return null;
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
