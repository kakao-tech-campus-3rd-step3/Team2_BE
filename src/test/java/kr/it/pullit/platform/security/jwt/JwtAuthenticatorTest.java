package kr.it.pullit.platform.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Optional;
import kr.it.pullit.modules.member.repository.MemberRepository;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import kr.it.pullit.platform.security.jwt.exception.JwtAuthenticationException;
import kr.it.pullit.platform.security.jwt.exception.TokenErrorCode;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import kr.it.pullit.support.fixture.MemberFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@MockitoUnitTest
@DisplayName("JwtAuthenticator 단위 테스트")
class JwtAuthenticatorTest {

  @InjectMocks private JwtAuthenticator jwtAuthenticator;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private DecodedJWT decodedJwt;

  @Mock private Claim memberIdClaim;

  @Mock private Claim emailClaim;

  @Mock private MemberRepository memberRepository;

  @Test
  @DisplayName("유효한 토큰이 제공되면 PullitAuthenticationToken을 반환한다")
  void shouldReturnSuccessWhenTokenIsValid() {
    // given
    String token = "valid-token";
    String expectedEmail = "tester@pullit.kr";

    when(jwtTokenProvider.validateAccessToken(token))
        .thenReturn(new TokenValidationResult.Valid(decodedJwt));
    when(decodedJwt.getClaim("memberId")).thenReturn(memberIdClaim);
    when(memberIdClaim.asLong()).thenReturn(1L);
    when(decodedJwt.getClaim("email")).thenReturn(emailClaim);
    when(emailClaim.asString()).thenReturn(expectedEmail);
    when(memberRepository.findById(1L)).thenReturn(Optional.of(MemberFixtures.basicUser()));

    // when
    PullitAuthenticationToken result = jwtAuthenticator.authenticate(token);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getPrincipal()).isEqualTo(1L);
    assertThat(result.getEmail()).isEqualTo(expectedEmail);
  }

  @Test
  @DisplayName("만료된 토큰이 제공되면 JwtAuthenticationException(EXPIRED)을 던진다")
  void shouldThrowExceptionWhenTokenIsExpired() {
    // given
    String token = "expired-token";
    when(jwtTokenProvider.validateAccessToken(token))
        .thenReturn(new TokenValidationResult.Expired());

    // when & then
    assertThatThrownBy(() -> jwtAuthenticator.authenticate(token))
        .isInstanceOf(JwtAuthenticationException.class)
        .hasFieldOrPropertyWithValue("errorCode", TokenErrorCode.TOKEN_EXPIRED);
  }

  @Test
  @DisplayName("유효하지 않은 토큰이 제공되면 JwtAuthenticationException(INVALID)을 던진다")
  void shouldThrowExceptionWhenTokenIsInvalid() {
    // given
    String token = "invalid-token";
    String errorMessage = "유효하지 않은 토큰입니다.";
    var cause = new RuntimeException("underlying cause");
    when(jwtTokenProvider.validateAccessToken(token))
        .thenReturn(new TokenValidationResult.Invalid(errorMessage, cause));

    // when & then
    assertThatThrownBy(() -> jwtAuthenticator.authenticate(token))
        .isInstanceOf(JwtAuthenticationException.class)
        .hasFieldOrPropertyWithValue("errorCode", TokenErrorCode.TOKEN_INVALID)
        .hasMessage(errorMessage);
  }

  @Test
  @DisplayName("토큰이 없으면 null을 반환한다")
  void shouldReturnNullWhenTokenIsNull() {
    // when
    PullitAuthenticationToken result = jwtAuthenticator.authenticate(null);

    // then
    assertThat(result).isNull();
  }
}
