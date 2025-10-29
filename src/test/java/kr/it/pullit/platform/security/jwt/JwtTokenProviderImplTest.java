package kr.it.pullit.platform.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.platform.security.jwt.dto.TokenCreationSubject;
import kr.it.pullit.support.annotation.SpringUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@SpringUnitTest
@DisplayName("JwtTokenProviderImpl 단위 테스트")
class JwtTokenProviderImplTest {

  @Autowired private Clock clock;

  private JwtTokenProviderImpl jwtTokenProvider;

  @BeforeEach
  void setUp() {
    JwtProps jwtProps =
        new JwtProps(
            "test-secret-key-that-is-long-enough-for-hs256-algorithm",
            "test-issuer",
            "test-audience",
            Duration.ofMinutes(30),
            Duration.ofDays(14),
            Collections.emptyList(),
            Collections.emptyList());
    jwtTokenProvider = new JwtTokenProviderImpl(jwtProps, clock);
  }

  @Test
  @DisplayName("고정된 시각을 기준으로 액세스 토큰을 생성하면, 정확한 만료 시간이 설정된다")
  void createAccessToken_WithFixedClock_ShouldSetCorrectTimestamps() {
    // given
    TokenCreationSubject subject = TokenCreationSubject.of(1L, "test@pullit.kr", Role.MEMBER);

    Instant expectedIssuedAt = Instant.parse("2025-01-01T00:00:00Z");
    Instant expectedExpiresAt = expectedIssuedAt.plus(Duration.ofMinutes(30));

    // when
    String accessToken = jwtTokenProvider.createAccessToken(subject);

    // then
    DecodedJWT decodedJWT = JWT.decode(accessToken);

    assertThat(decodedJWT.getIssuedAt()).isEqualTo(expectedIssuedAt);
    assertThat(decodedJWT.getExpiresAt()).isEqualTo(expectedExpiresAt);
    assertThat(decodedJWT.getClaim("memberId").asLong()).isEqualTo(1L);
    assertThat(decodedJWT.getClaim("email").asString()).isEqualTo("test@pullit.kr");
    assertThat(decodedJWT.getClaim("role").asString()).isEqualTo("MEMBER");
  }
}
