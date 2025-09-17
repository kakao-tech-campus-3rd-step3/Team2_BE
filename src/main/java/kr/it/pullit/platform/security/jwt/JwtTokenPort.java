package kr.it.pullit.platform.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import org.springframework.security.core.Authentication;

public interface JwtTokenPort {

  AuthTokens createAuthTokens(Member member);

  String createAccessToken(Member member);

  String createRefreshToken(Member member);

  TokenValidationResult validateToken(String token);

  Authentication getAuthentication(String token);

  String resolveToken(HttpServletRequest request);
}
