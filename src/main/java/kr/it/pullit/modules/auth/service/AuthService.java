package kr.it.pullit.modules.auth.service;

import kr.it.pullit.modules.auth.domain.entity.RefreshToken;
import kr.it.pullit.modules.auth.exception.InvalidRefreshTokenException;
import kr.it.pullit.modules.auth.repository.RefreshTokenRepository;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.platform.security.jwt.JwtProps;
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
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProps jwtProps;

  @Transactional
  public AuthTokens issueAndSaveTokens(Long memberId) {
    Member member =
        memberPublicApi
            .findById(memberId)
            .orElseThrow(() -> MemberNotFoundException.byId(memberId));

    AuthTokens newAuthTokens = jwtTokenProvider.createAuthTokens(TokenCreationSubject.from(member));
    storeRefreshToken(member, newAuthTokens.refreshToken());

    return newAuthTokens;
  }

  public void storeRefreshToken(Member member, String refreshToken) {
    long ttl = jwtProps.refreshTokenExpirationDays().toSeconds();

    RefreshToken tokenEntity =
        RefreshToken.of(member.getId(), refreshToken, member.getEmail(), member.getRole(), ttl);
    refreshTokenRepository.save(tokenEntity);
    log.info(" [리프레시 토큰 저장] Redis Key(memberId): {}", member.getId());
  }

  @Transactional(readOnly = true)
  public String reissueAccessToken(String refreshToken) {
    log.info(" [토큰 갱신] API로 전달받은 리프레시 토큰: {}", refreshToken);
    validateRefreshToken(refreshToken);

    RefreshToken tokenEntity =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(InvalidRefreshTokenException::by);

    TokenCreationSubject subject =
        TokenCreationSubject.of(
            tokenEntity.getMemberId(), tokenEntity.getEmail(), Role.valueOf(tokenEntity.getRole()));

    return jwtTokenProvider.createAccessToken(subject);
  }

  @Transactional
  public void logout(Long memberId) {
    refreshTokenRepository.deleteById(memberId);
    log.info(" [로그아웃] Redis에서 리프레시 토큰 삭제 완료. Key(memberId): {}", memberId);
  }

  private void validateRefreshToken(String refreshToken) {
    if (!jwtTokenProvider.validateRefreshToken(refreshToken).isValid()) {
      throw InvalidRefreshTokenException.by();
    }
  }
}
