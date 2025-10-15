package kr.it.pullit.platform.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import kr.it.pullit.support.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@UnitTest
@DisplayName("JwtAuthenticator 단위 테스트")
class JwtAuthenticatorTest {

  @InjectMocks private JwtAuthenticator jwtAuthenticator;

  @Mock private JwtTokenPort jwtTokenPort;

  @Mock private DecodedJWT decodedJwt;

  @Mock private Claim memberIdClaim;

  @Mock private Claim emailClaim;

  @Test
  @DisplayName("유효한 토큰이 제공되면 Success 결과를 반환한다")
  void shouldReturnSuccessWhenTokenIsValid() {
    // given
    String token = "valid-token";
    String expectedEmail = "tester@pullit.kr";

    when(jwtTokenPort.validateToken(token)).thenReturn(new TokenValidationResult.Valid(decodedJwt));
    when(decodedJwt.getClaim("memberId")).thenReturn(memberIdClaim);
    when(memberIdClaim.asLong()).thenReturn(1L);
    when(decodedJwt.getClaim("email")).thenReturn(emailClaim);
    when(emailClaim.asString()).thenReturn(expectedEmail);

    // when
    AuthenticationResult result = jwtAuthenticator.authenticate(token);

    // then
    assertThat(result).isInstanceOf(AuthenticationResult.Success.class);
    var successResult = (AuthenticationResult.Success) result;
    assertThat(successResult.authentication().getPrincipal()).isEqualTo(1L);
    assertThat(successResult.authentication().getEmail()).isEqualTo(expectedEmail);
  }

  @Test
  @DisplayName("만료된 토큰이 제공되면 Expired 결과를 반환한다")
  void shouldReturnExpiredWhenTokenIsExpired() {
    // given
    String token = "expired-token";
    when(jwtTokenPort.validateToken(token)).thenReturn(new TokenValidationResult.Expired());

    // when
    AuthenticationResult result = jwtAuthenticator.authenticate(token);

    // then
    assertThat(result).isInstanceOf(AuthenticationResult.Expired.class);
  }

  @Test
  @DisplayName("유효하지 않은 토큰이 제공되면 Invalid 결과를 반환한다")
  void shouldReturnInvalidWhenTokenIsInvalid() {
    // given
    String token = "invalid-token";
    when(jwtTokenPort.validateToken(token)).thenReturn(new TokenValidationResult.Invalid("error"));

    // when
    AuthenticationResult result = jwtAuthenticator.authenticate(token);

    // then
    assertThat(result).isInstanceOf(AuthenticationResult.Invalid.class);
  }

  @Test
  @DisplayName("토큰이 없으면 NoToken 결과를 반환한다")
  void shouldReturnNoTokenWhenTokenIsNull() {
    // when
    AuthenticationResult result = jwtAuthenticator.authenticate(null);

    // then
    assertThat(result).isInstanceOf(AuthenticationResult.NoToken.class);
  }
}
