package kr.it.pullit.platform.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;

public interface JwtTokenPort {

  AuthTokens createAuthTokens(Long memberId, String email, Role role);

  String createAccessToken(Long memberId, String email, Role role);

  String createRefreshToken(Long memberId, String email, Role role);

  TokenValidationResult validateToken(String token);

  String resolveToken(HttpServletRequest request);
}
