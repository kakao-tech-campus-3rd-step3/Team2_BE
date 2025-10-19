package kr.it.pullit.modules.auth.service;

import kr.it.pullit.modules.auth.exception.InvalidRefreshTokenException;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.platform.security.jwt.JwtTokenProvider;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenCreationSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  // TODO: REVIEW-3 : 메서드를 기능 단위로 추출해보세요. 그리고 값을 꺼내서 처리하고 있다면 Value Object를 도출하거나 객체에게 메시지를 보내보세요.

  private final MemberPublicApi memberPublicApi;
  private final JwtTokenProvider jwtTokenPort;

  @Transactional
  public AuthTokens issueAndSaveTokens(Long memberId) {
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> MemberNotFoundException.byId(memberId));

    String existingRefreshToken = member.getRefreshToken();

    // 기존 리프레시 토큰이 있고, 유효하다면 재사용
    if (jwtTokenPort.validateToken(existingRefreshToken).isValid()) {
      String newAccessToken = jwtTokenPort.createAccessToken(TokenCreationSubject.from(member));
      return new AuthTokens(newAccessToken, existingRefreshToken);
    }

    // 기존 토큰이 없거나 유효하지 않으면 새로 발급
    AuthTokens newAuthTokens = jwtTokenPort.createAuthTokens(TokenCreationSubject.from(member));
    member.updateRefreshToken(newAuthTokens.refreshToken());

    return newAuthTokens;
  }

  @Transactional(readOnly = true)
  public String reissueAccessToken(String refreshToken) {
    validateRefreshToken(refreshToken);

    Member member =
        memberPublicApi
            .findByRefreshToken(refreshToken)
            .orElseThrow(InvalidRefreshTokenException::by);

    return jwtTokenPort.createAccessToken(TokenCreationSubject.from(member));
  }

  @Transactional
  public void logout(Long memberId) {
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> MemberNotFoundException.byId(memberId));
    member.updateRefreshToken(null);
  }

  private void validateRefreshToken(String refreshToken) {
    if (!jwtTokenPort.validateToken(refreshToken).isValid()) {
      throw InvalidRefreshTokenException.by();
    }
  }
}
