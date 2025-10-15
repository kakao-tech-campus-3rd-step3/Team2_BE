package kr.it.pullit.integration.modules.member.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.web.MemberController;
import kr.it.pullit.modules.member.web.dto.MemberInfoResponse;
import kr.it.pullit.platform.security.jwt.JwtAuthenticationFilter;
import kr.it.pullit.platform.web.cookie.CookieManager;
import kr.it.pullit.support.test.MvcSliceTest;
import kr.it.pullit.support.test.security.WithMockMember;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@MvcSliceTest(
    controllers = MemberController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@DisplayName("MemberController 슬라이스 테스트")
class MemberControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private MemberPublicApi memberPublicApi;

  @MockitoBean private CookieManager cookieManager;

  @Test
  @WithMockMember
  @DisplayName("로그인한 사용자는 자신의 정보를 성공적으로 조회한다")
  void shouldSuccessfullyRetrieveMyInfoWhenLoggedIn() throws Exception {
    // given
    var memberInfo = new MemberInfoResponse(1L, "테스터", "test@pullit.kr");
    given(memberPublicApi.getMemberInfo(1L)).willReturn(Optional.of(memberInfo));

    // when & then
    mockMvc
        .perform(get("/api/members/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.name").value("테스터"));
  }

  @Test
  @WithMockMember(memberId = 999L)
  @DisplayName("존재하지 않는 사용자의 정보를 조회하면 404를 반환한다")
  void shouldReturn404WhenMemberInfoDoesNotExist() throws Exception {
    // given
    given(memberPublicApi.getMemberInfo(999L)).willReturn(Optional.empty());

    // when & then
    mockMvc.perform(get("/api/members/me")).andExpect(status().isNotFound());
  }
}
