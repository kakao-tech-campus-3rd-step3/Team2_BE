package kr.it.pullit.modules.auth.service;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.platform.security.exception.InvalidRefreshTokenException;
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

  // TODO: REVIEW-3 : 메서드를 기능 단위로 추출해보세요. 그리고 값을 꺼내서 처리하고 있다면 Value Object를 도출하거나 객체에게 메시지를 보내보세요.

  private final MemberPublicApi memberPublicApi;
  private final JwtTokenPort jwtTokenPort;

  @Transactional
  public AuthTokens issueAndSaveTokens(Long memberId) {
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find member by id"));

    String existingRefreshToken = member.getRefreshToken();

    // 기존 리프레시 토큰이 있고, 유효하다면 재사용
    if (StringUtils.hasText(existingRefreshToken)
        && jwtTokenPort.validateToken(existingRefreshToken)
            instanceof TokenValidationResult.Valid) {
      String newAccessToken =
          jwtTokenPort.createAccessToken(member.getId(), member.getEmail(), Role.USER);
      return new AuthTokens(newAccessToken, existingRefreshToken);
    }

    // 기존 토큰이 없거나 유효하지 않으면 새로 발급
    AuthTokens newAuthTokens =
        jwtTokenPort.createAuthTokens(member.getId(), member.getEmail(), Role.USER);
    member.updateRefreshToken(newAuthTokens.refreshToken());

    return newAuthTokens;
  }

  @Transactional(readOnly = true)
  public String reissueAccessToken(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)) {
      throw new InvalidRefreshTokenException();
    }

    TokenValidationResult result = jwtTokenPort.validateToken(refreshToken);
    if (result instanceof TokenValidationResult.Invalid) {
      throw new InvalidRefreshTokenException();
    }
    if (result instanceof TokenValidationResult.Expired) {
      throw new InvalidRefreshTokenException();
    }

    Member member =
        memberPublicApi
            .findByRefreshToken(refreshToken)
            .orElseThrow(InvalidRefreshTokenException::new);

    return jwtTokenPort.createAccessToken(member.getId(), member.getEmail(), Role.USER);
  }

  @Transactional
  public void logout(Long memberId) {
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find member by id"));
    member.updateRefreshToken(null);
  }
}
