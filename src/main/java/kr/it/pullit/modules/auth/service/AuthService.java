package kr.it.pullit.modules.auth.service;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.platform.security.jwt.JwtTokenPort;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final MemberPublicApi memberPublicApi;
  private final JwtTokenPort jwtTokenPort;

  @Transactional
  public AuthTokens issueAndSaveTokens(Member member) {
    AuthTokens authTokens =
        jwtTokenPort.createAuthTokens(member.getId(), member.getEmail(), Role.USER);

    member.updateRefreshToken(authTokens.refreshToken());
    memberPublicApi.save(member);

    return authTokens;
  }

  @Transactional(readOnly = true)
  public String reissueAccessToken(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)) {
      throw new IllegalArgumentException("Refresh token is empty");
    }

    TokenValidationResult result = jwtTokenPort.validateToken(refreshToken);
    if (result instanceof TokenValidationResult.Invalid(String cause)) {
      throw new IllegalArgumentException("Invalid refresh token: " + cause);
    }
    if (result instanceof TokenValidationResult.Expired) {
      throw new IllegalArgumentException("Refresh token has expired.");
    }

    Member member =
        memberPublicApi
            .findByRefreshToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find member by refresh token"));

    return jwtTokenPort.createAccessToken(member.getId(), member.getEmail(), Role.USER);
  }

  @Transactional
  public void logout(Long memberId) {
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find member by id"));
    member.updateRefreshToken(null);
    memberPublicApi.save(member);
  }
}
