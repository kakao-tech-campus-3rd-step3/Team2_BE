package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.List;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import kr.it.pullit.platform.security.jwt.exception.TokenErrorCode;
import kr.it.pullit.platform.security.jwt.exception.TokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticator {

  private final JwtTokenProvider jwtTokenPort;
  private final MemberRepository memberRepository;

  public AuthenticationResult authenticate(String token) {
    if (!StringUtils.hasText(token)) {
      return new AuthenticationResult.NoToken();
    }

    TokenValidationResult validationResult = jwtTokenPort.validateAccessToken(token);

    return handleValidationResult(validationResult);
  }

  private AuthenticationResult handleValidationResult(TokenValidationResult validationResult) {
    return switch (validationResult) {
      case TokenValidationResult.Valid(DecodedJWT decodedJwt) -> processValidToken(decodedJwt);
      case TokenValidationResult.Expired ignored -> handleExpiredToken();
      case TokenValidationResult.Invalid(String message, Throwable cause) ->
          handleInvalidToken(message, cause);
    };
  }

  private AuthenticationResult handleInvalidToken(String message, Throwable cause) {
    throw new TokenException(TokenErrorCode.TOKEN_INVALID, message, cause);
  }

  private AuthenticationResult handleExpiredToken() {
    throw new TokenException(TokenErrorCode.TOKEN_EXPIRED);
  }

  private AuthenticationResult processValidToken(DecodedJWT decodedJwt) {
    Long memberId = decodedJwt.getClaim("memberId").asLong();
    Member member = findMemberById(memberId);

    PullitAuthenticationToken authentication = createAuthenticationToken(decodedJwt, member);
    return new AuthenticationResult.Success(authentication);
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
