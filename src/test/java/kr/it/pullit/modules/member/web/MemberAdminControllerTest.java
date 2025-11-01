package kr.it.pullit.modules.member.web;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = MemberAdminController.class)
@DisplayName("MemberAdminController 슬라이스 테스트")
class MemberAdminControllerTest extends ControllerTest {

  @MockitoBean private MemberPublicApi memberPublicApi;

  @Test
  @WithMockMember(role = Role.ADMIN)
  @DisplayName("[성공] 어드민은 사용자에게 어드민 권한을 부여할 수 있다")
  void shouldGrantAdminRoleByAdmin() throws Exception {
    // given
    long targetMemberId = 2L;

    // when & then
    mockMvc
        .perform(post("/api/admin/members/{id}/grant-admin", targetMemberId))
        .andExpect(status().isOk());

    verify(memberPublicApi).grantAdminRole(targetMemberId);
  }

  @Test
  @WithMockMember(role = Role.MEMBER)
  @DisplayName("[실패] 일반 사용자는 다른 사용자에게 어드민 권한을 부여할 수 없다")
  void shouldNotGrantAdminRoleByUser() throws Exception {
    // given
    long targetMemberId = 2L;

    // when & then
    mockMvc
        .perform(post("/api/admin/members/{id}/grant-admin", targetMemberId))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockMember(role = Role.ADMIN)
  @DisplayName("[성공] 어드민은 어드민 사용자의 권한을 회수할 수 있다")
  void shouldRevokeAdminRoleByAdmin() throws Exception {
    // given
    long targetMemberId = 2L;

    // when & then
    mockMvc
        .perform(post("/api/admin/members/{id}/revoke-admin", targetMemberId))
        .andExpect(status().isOk());

    verify(memberPublicApi).revokeAdminRole(targetMemberId);
  }
}
