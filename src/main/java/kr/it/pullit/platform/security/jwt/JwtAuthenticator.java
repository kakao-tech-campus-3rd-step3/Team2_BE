package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.List;
import kr.it.pullit.modules.auth.exception.AuthErrorCode;
import kr.it.pullit.modules.auth.exception.InvalidAccessTokenException;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import kr.it.pullit.platform.security.jwt.exception.JwtAuthenticationException;
import kr.it.pullit.platform.security.jwt.exception.TokenErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticator {

  private final JwtTokenProvider jwtTokenProvider;
  private final MemberRepository memberRepository;

  public PullitAuthenticationToken authenticate(String token) {
    if (!StringUtils.hasText(token)) {
      return null;
    }

    try {
      TokenValidationResult validationResult = jwtTokenProvider.validateAccessToken(token);
      return handleValidationResult(validationResult);
    } catch (InvalidAccessTokenException e) {
      throw JwtAuthenticationException.from(AuthErrorCode.INVALID_ACCESS_TOKEN);
    }
  }

  private PullitAuthenticationToken handleValidationResult(TokenValidationResult validationResult) {
    return switch (validationResult) {
      case TokenValidationResult.Valid(var decodedJwt) -> processValidToken(decodedJwt);
      case TokenValidationResult.Expired ignored ->
          throw JwtAuthenticationException.from(TokenErrorCode.TOKEN_EXPIRED);
      case TokenValidationResult.Invalid(var errorMessage, var cause) ->
          throw JwtAuthenticationException.withMessage(TokenErrorCode.TOKEN_INVALID, errorMessage);
    };
  }

  private PullitAuthenticationToken processValidToken(DecodedJWT decodedJwt) {
    Long memberId = decodedJwt.getClaim("memberId").asLong();
    Member member = findMemberById(memberId);

    return createAuthenticationToken(decodedJwt, member);
  }

  private PullitAuthenticationToken createAuthenticationToken(
      DecodedJWT decodedJwt, Member member) {
    Long memberId = decodedJwt.getClaim("memberId").asLong();
    String email = decodedJwt.getClaim("email").asString();
    List<SimpleGrantedAuthority> authorities = member.getRole().getAuthorities();

    return new PullitAuthenticationToken(memberId, email, decodedJwt, authorities);
  }

  private Member findMemberById(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> MemberNotFoundException.byId(memberId));
  }
}
