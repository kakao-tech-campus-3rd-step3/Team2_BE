package kr.it.pullit.platform.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collections;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.platform.security.jwt.dto.TokenCreationSubject;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@MockitoUnitTest
@DisplayName("JwtTokenProviderImpl 단위 테스트")
class JwtTokenProviderImplTest {

  private JwtTokenProviderImpl jwtTokenProviderImpl;

  @BeforeEach
  void setUp() {
    JwtProps jwtProps =
        new JwtProps(
            "test-secret-key-with-sufficient-length-for-hs256",
            "pullit-backend-test",
            "pullit-client-test",
            Duration.ofMinutes(30),
            Duration.ofDays(7),
            Collections.emptyList(),
            Collections.emptyList());
    jwtTokenProviderImpl = new JwtTokenProviderImpl(jwtProps);
  }

  @Test
  @DisplayName("AccessToken을 성공적으로 생성하고 검증한다")
  void shouldCreateAndValidateAccessToken() {
    // given
    TokenCreationSubject subject = new TokenCreationSubject(1L, "test@pullit.kr", Role.MEMBER);

    // when
    String accessToken = jwtTokenProviderImpl.createAccessToken(subject);
    TokenValidationResult result = jwtTokenProviderImpl.validateAccessToken(accessToken);

    // then
    assertThat(result).isInstanceOf(TokenValidationResult.Valid.class);
    var validResult = (TokenValidationResult.Valid) result;
    assertThat(validResult.decodedJwt().getSubject()).isEqualTo("test@pullit.kr");
    assertThat(validResult.decodedJwt().getClaim("memberId").asLong()).isEqualTo(1L);
    assertThat(validResult.decodedJwt().getClaim("role").asString()).isEqualTo("MEMBER");
  }

  @Test
  @DisplayName("만료된 토큰을 검증하면 Expired 결과를 반환한다")
  void shouldReturnExpiredWhenTokenIsExpired() {
    // given
    JwtProps jwtProps =
        new JwtProps(
            "test-secret-key-with-sufficient-length-for-hs256",
            "pullit-backend-test",
            "pullit-client-test",
            Duration.ZERO,
            Duration.ZERO,
            Collections.emptyList(),
            Collections.emptyList());
    JwtTokenProviderImpl expiredTokenProvider = new JwtTokenProviderImpl(jwtProps);
    TokenCreationSubject subject = new TokenCreationSubject(1L, "test@pullit.kr", Role.MEMBER);
    String expiredToken = expiredTokenProvider.createAccessToken(subject);

    // when
    TokenValidationResult result = jwtTokenProviderImpl.validateAccessToken(expiredToken);

    // then
    assertThat(result).isInstanceOf(TokenValidationResult.Expired.class);
  }

  @Test
  @DisplayName("유효하지 않은 토큰을 검증하면 Invalid 결과를 반환한다")
  void shouldReturnInvalidWhenTokenIsInvalid() {
    // given
    String invalidToken = "invalid-token";

    // when
    TokenValidationResult result = jwtTokenProviderImpl.validateAccessToken(invalidToken);

    // then
    assertThat(result).isInstanceOf(TokenValidationResult.Invalid.class);
  }
}
