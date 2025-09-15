package kr.it.pullit.modules.auth.service;

import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.platform.security.jwt.JwtTokenPort;
import kr.it.pullit.platform.security.jwt.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final MemberRepository memberRepository;
  private final JwtTokenPort jwtTokenPort;

  @Transactional(readOnly = true)
  public String reissueAccessToken(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)) {
      throw new IllegalArgumentException("Refresh token is empty");
    }

    switch (jwtTokenPort.validateToken(refreshToken)) {
      case TokenValidationResult.Invalid(var message) ->
          throw new IllegalArgumentException("Invalid refresh token: " + message);
      case TokenValidationResult.Expired() ->
          throw new IllegalArgumentException("Refresh token has expired.");
      case TokenValidationResult.Valid(var ignored) -> {} // Proceed
    }

    Member member =
        memberRepository
            .findByRefreshToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find member by refresh token"));

    // TODO: Role 관리 정책 수립되면 수정 필요. 현재는 모든 소셜 로그인 유저를 USER로 간주.
    return jwtTokenPort.createAccessToken(member.getId(), member.getEmail(), Role.USER);
  }

  @Transactional
  public void logout(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find member by id"));
    member.updateRefreshToken(null);
    memberRepository.save(member);
  }
}
