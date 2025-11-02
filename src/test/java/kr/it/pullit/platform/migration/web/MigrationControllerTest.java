package kr.it.pullit.platform.migration.web;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.it.pullit.modules.member.domain.entity.Role;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsRecalibrationPublicApi;
import kr.it.pullit.platform.migration.api.MigrationPublicApi;
import kr.it.pullit.support.annotation.AuthenticatedMvcSliceTest;
import kr.it.pullit.support.security.WithMockMember;
import kr.it.pullit.support.test.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AuthenticatedMvcSliceTest(controllers = MigrationController.class)
@DisplayName("MigrationController 슬라이스 테스트")
class MigrationControllerTest extends ControllerTest {

  @MockitoBean private MigrationPublicApi migrationPublicApi;
  @MockitoBean private LearnStatsRecalibrationPublicApi recalibrationApi;

  @Test
  @WithMockMember(role = Role.ADMIN)
  @DisplayName("[성공] 어드민은 데이터 마이그레이션을 실행할 수 있다")
  void shouldRunMigrationByAdmin() throws Exception {
    // when & then
    mockMvc.perform(post("/api/admin/migrations/source-status-v1")).andExpect(status().isOk());

    verify(migrationPublicApi).runSourceStatusMigration();
  }

  @Test
  @WithMockMember(role = Role.ADMIN)
  @DisplayName("[성공] 어드민은 학습 통계 보정 작업을 실행할 수 있다")
  void shouldRunLearnStatsRecalibrationByAdmin() throws Exception {
    // when & then
    mockMvc
        .perform(post("/api/admin/migrations/recalibrate/learn-stats"))
        .andExpect(status().isOk());

    verify(recalibrationApi).recalibrateAllMembers();
  }

  @Test
  @WithMockMember(role = Role.MEMBER)
  @DisplayName("[실패] 일반 사용자는 데이터 마이그레이션을 실행할 수 없다")
  void shouldNotRunMigrationByUser() throws Exception {
    // when & then
    mockMvc
        .perform(post("/api/admin/migrations/source-status-v1"))
        .andExpect(status().isForbidden());
  }
}
