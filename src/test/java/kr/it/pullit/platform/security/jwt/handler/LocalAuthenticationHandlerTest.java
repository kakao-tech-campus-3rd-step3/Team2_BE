package kr.it.pullit.platform.security.jwt.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import jakarta.servlet.http.HttpServletRequest;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.platform.security.jwt.PullitAuthenticationToken;
import kr.it.pullit.platform.security.jwt.filter.HeaderHidingRequestWrapper;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContextHolder;

@MockitoUnitTest
@DisplayName("LocalAuthenticationHandler 단위 테스트")
class LocalAuthenticationHandlerTest {

  @InjectMocks private LocalAuthenticationHandler localAuthenticationHandler;

  @Mock private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  @DisplayName("authenticate")
  class DescribeAuthenticate {

    @Test
    @DisplayName("Authorization 헤더가 'Bearer 1'이면 개발자 인증을 적용하고 HeaderHidingRequestWrapper를 반환한다")
    void appliesDevAuthenticationWhenBearerOne() {
      // given
      given(request.getHeader("Authorization")).willReturn("Bearer 1");

      // when
      HttpServletRequest result = localAuthenticationHandler.authenticate(request);

      // then
      assertThat(result).isInstanceOf(HeaderHidingRequestWrapper.class);
      var authentication = SecurityContextHolder.getContext().getAuthentication();
      assertThat(authentication).isInstanceOf(PullitAuthenticationToken.class);
      var token = (PullitAuthenticationToken) authentication;
      assertThat(token.getPrincipal()).isEqualTo(1L);
      assertThat(token.getEmail()).isEqualTo("dev-user@pullit.kr");
      assertThat(token.getAuthorities()).containsExactlyElementsOf(Role.ADMIN.getAuthorities());
    }

    @Test
    @DisplayName("Authorization 헤더가 'Bearer 2'이면 개발자 인증을 적용하지 않고 원래 request를 반환한다")
    void doesNotApplyDevAuthenticationWhenBearerTwo() {
      // given
      given(request.getHeader("Authorization")).willReturn("Bearer 2");

      // when
      HttpServletRequest result = localAuthenticationHandler.authenticate(request);

      // then
      assertThat(result).isSameAs(request);
      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 개발자 인증을 적용하지 않고 원래 request를 반환한다")
    void doesNotApplyDevAuthenticationWhenNoHeader() {
      // given
      given(request.getHeader("Authorization")).willReturn(null);

      // when
      HttpServletRequest result = localAuthenticationHandler.authenticate(request);

      // then
      assertThat(result).isSameAs(request);
      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 'Bearer '로 시작하지 않으면 개발자 인증을 적용하지 않고 원래 request를 반환한다")
    void doesNotApplyDevAuthenticationWhenHeaderDoesNotStartWithBearer() {
      // given
      given(request.getHeader("Authorization")).willReturn("Token 1");

      // when
      HttpServletRequest result = localAuthenticationHandler.authenticate(request);

      // then
      assertThat(result).isSameAs(request);
      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 'Bearer '로 시작하지만 값이 '1'이 아니면 개발자 인증을 적용하지 않고 원래 request를 반환한다")
    void doesNotApplyDevAuthenticationWhenTokenIsNotOne() {
      // given
      given(request.getHeader("Authorization")).willReturn("Bearer 123");

      // when
      HttpServletRequest result = localAuthenticationHandler.authenticate(request);

      // then
      assertThat(result).isSameAs(request);
      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
  }
}
