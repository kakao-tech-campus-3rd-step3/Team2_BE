package kr.it.pullit.modules.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.auth.domain.entity.RefreshToken;
import kr.it.pullit.modules.member.domain.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.test.context.TestPropertySource;

@DataRedisTest
@TestPropertySource(properties = {"spring.data.redis.host=localhost"})
@DisplayName("RefreshTokenRepository 슬라이스 테스트")
class RefreshTokenRepositoryTest {

  @Autowired private RefreshTokenRepository refreshTokenRepository;

  @Test
  @DisplayName("RefreshToken을 저장하고 토큰 값으로 조회하면, 저장된 토큰이 조회되어야 한다")
  void shouldSaveAndFindRefreshTokenByToken() {
    // given
    long memberId = 1L;
    String tokenValue = "test-refresh-token";
    String email = "test@example.com";
    Role role = Role.MEMBER;
    long expiration = 1000L;

    RefreshToken refreshToken = RefreshToken.of(memberId, tokenValue, email, role, expiration);

    // when
    refreshTokenRepository.save(refreshToken);
    RefreshToken foundToken = refreshTokenRepository.findByToken(tokenValue).orElse(null);

    // then
    assertThat(foundToken).isNotNull();
    assertThat(foundToken.getMemberId()).isEqualTo(memberId);
    assertThat(foundToken.getToken()).isEqualTo(tokenValue);
    assertThat(foundToken.getEmail()).isEqualTo(email);
    assertThat(foundToken.getRole()).isEqualTo(role.name());
  }
}
