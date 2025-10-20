package kr.it.pullit.modules.auth.service;

import kr.it.pullit.modules.auth.exception.InvalidRefreshTokenException;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.platform.security.jwt.JwtTokenProvider;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenCreationSubject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final MemberPublicApi memberPublicApi;
  private final JwtTokenProvider jwtTokenPort;

  @Transactional
  public AuthTokens issueAndSaveTokens(Long memberId) {
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> MemberNotFoundException.byId(memberId));

    AuthTokens newAuthTokens = jwtTokenPort.createAuthTokens(TokenCreationSubject.from(member));
    member.updateRefreshToken(newAuthTokens.refreshToken());

    log.info(" [토큰 발급] DB에 저장된 리프레시 토큰: {}", newAuthTokens.refreshToken());

    return newAuthTokens;
  }

  @Transactional(readOnly = true)
  public String reissueAccessToken(String refreshToken) {
    log.info(" [토큰 갱신] API로 전달받은 리프레시 토큰: {}", refreshToken);
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
