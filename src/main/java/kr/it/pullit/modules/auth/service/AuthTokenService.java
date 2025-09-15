package kr.it.pullit.modules.auth.service;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.platform.security.jwt.JwtTokenPort;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

  private final JwtTokenPort jwtTokenPort;
  private final MemberRepository memberRepository;

  @Transactional
  public AuthTokens issueAndSaveTokens(Member member) {
    AuthTokens authTokens =
        jwtTokenPort.createAuthTokens(member.getId(), member.getEmail(), Role.USER);

    member.updateRefreshToken(authTokens.refreshToken());
    memberRepository.save(member);

    return authTokens;
  }
}
