package kr.it.pullit.platform.security.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Collections;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
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

    TokenValidationResult validationResult = jwtTokenPort.validateToken(token);

    return switch (validationResult) {
      case TokenValidationResult.Valid(DecodedJWT decodedJwt) -> {
        Long memberId = decodedJwt.getClaim("memberId").asLong();
        String email = decodedJwt.getClaim("email").asString();

        Member member =
            memberRepository
                .findById(memberId)
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "JWT 토큰은 유효하지만 ID '%d'에 해당하는 멤버를 찾을 수 없습니다.".formatted(memberId)));

        var authorities =
            Collections.singletonList(new SimpleGrantedAuthority(member.getRole().getKey()));

        var authentication =
            new PullitAuthenticationToken(memberId, email, decodedJwt, authorities);
        yield new AuthenticationResult.Success(authentication);
      }
      case TokenValidationResult.Expired ignored -> new AuthenticationResult.Expired();
      case TokenValidationResult.Invalid(String message) ->
          new AuthenticationResult.Invalid(message);
    };
  }
}
