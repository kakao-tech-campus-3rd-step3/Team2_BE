package kr.it.pullit.modules.auth.service;

import static kr.it.pullit.support.fixture.MemberFixtures.basicUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Optional;
import kr.it.pullit.modules.auth.domain.entity.RefreshToken;
import kr.it.pullit.modules.auth.exception.InvalidRefreshTokenException;
import kr.it.pullit.modules.auth.repository.RefreshTokenRepository;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.platform.security.jwt.JwtProps;
import kr.it.pullit.platform.security.jwt.JwtTokenProvider;
import kr.it.pullit.platform.security.jwt.dto.AuthTokens;
import kr.it.pullit.platform.security.jwt.dto.TokenValidationResult;
import kr.it.pullit.support.annotation.SpringUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringUnitTest
@ContextConfiguration(classes = AuthService.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

  @Autowired private AuthService authService;

  @MockitoBean private MemberPublicApi memberPublicApi;
  @MockitoBean private JwtTokenProvider jwtTokenProvider;
  @MockitoBean private RefreshTokenRepository refreshTokenRepository;
  @MockitoBean private JwtProps jwtProps;

  @Nested
  @DisplayName("토큰 발급 (issueAndSaveTokens)")
  class IssueAndSaveTokens {

    @Test
    @DisplayName("사용자가 존재하면, 새로운 토큰을 발급하고 리프레시 토큰을 저장한다")
    void givenMemberExistsIssuesAndSavesTokens() {
      // given
      Member member = basicUser();
      Long memberId = member.getId();
      AuthTokens authTokens = new AuthTokens("newAccessToken", "newRefreshToken");

      given(memberPublicApi.findById(memberId)).willReturn(Optional.of(member));
      given(jwtTokenProvider.createAuthTokens(any())).willReturn(authTokens);
      given(jwtProps.refreshTokenExpirationDays()).willReturn(Duration.ofDays(14));

      // when
      AuthTokens result = authService.issueAndSaveTokens(memberId);

      // then
      assertThat(result).isEqualTo(authTokens);
      verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면, MemberNotFoundException 예외를 던진다")
    void givenMemberNotFoundThrowsMemberNotFoundException() {
      // given
      Long memberId = 999L;
      given(memberPublicApi.findById(memberId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.issueAndSaveTokens(memberId))
          .isInstanceOf(MemberNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("액세스 토큰 재발급 (reissueAccessToken)")
  class ReissueAccessToken {

    @Test
    @DisplayName("유효한 리프레시 토큰으로, 새로운 액세스 토큰을 재발급한다")
    void givenValidRefreshTokenReissuesAccessToken() {
      // given
      String refreshToken = "validRefreshToken";
      RefreshToken tokenEntity =
          RefreshToken.of(1L, refreshToken, "test@test.com", Role.MEMBER, 600L);

      given(jwtTokenProvider.validateRefreshToken(refreshToken))
          .willReturn(new TokenValidationResult.Valid(null));
      given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(tokenEntity));
      given(jwtTokenProvider.createAccessToken(any())).willReturn("newAccessToken");

      // when
      String result = authService.reissueAccessToken(refreshToken);
      // then
      assertThat(result).isEqualTo("newAccessToken");
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰이면, InvalidRefreshTokenException 예외를 던진다")
    void givenInvalidRefreshTokenThrowsInvalidRefreshTokenException() {
      // given
      String refreshToken = "invalidRefreshToken";
      given(jwtTokenProvider.validateRefreshToken(refreshToken))
          .willReturn(new TokenValidationResult.Invalid("Invalid token"));

      // when & then
      assertThatThrownBy(() -> authService.reissueAccessToken(refreshToken))
          .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("저장소에 존재하지 않는 리프레시 토큰이면, InvalidRefreshTokenException 예외를 던진다")
    void givenNonExistentRefreshTokenThrowsInvalidRefreshTokenException() {
      // given
      String refreshToken = "nonExistentRefreshToken";
      given(jwtTokenProvider.validateRefreshToken(refreshToken))
          .willReturn(new TokenValidationResult.Valid(null));
      given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> authService.reissueAccessToken(refreshToken))
          .isInstanceOf(InvalidRefreshTokenException.class);
    }
  }

  @Nested
  @DisplayName("로그아웃 (logout)")
  class Logout {

    @Test
    @DisplayName("사용자의 리프레시 토큰을 저장소에서 삭제한다")
    void givenUserDeletesRefreshToken() {
      // given
      Long memberId = 1L;

      // when
      authService.logout(memberId);

      // then
      verify(refreshTokenRepository).deleteById(memberId);
    }
  }
}
