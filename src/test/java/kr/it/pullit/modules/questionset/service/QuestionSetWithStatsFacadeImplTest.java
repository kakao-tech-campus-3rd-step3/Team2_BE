package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithStatsResponse;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoUnitTest
@DisplayName("QuestionSetWithStatsFacadeImpl 단위 테스트")
class QuestionSetWithStatsFacadeImplTest {

  @InjectMocks private QuestionSetWithStatsFacadeImpl questionSetWithStatsFacade;

  @Mock private QuestionSetPublicApi questionSetPublicApi;

  @Mock private LearnStatsPublicApi learnStatsPublicApi;

  @Mock private CommonFolderPublicApi commonFolderPublicApi;

  @Test
  @DisplayName("folderId가 null이면, 폴더 필터링 없이 모든 문제집을 조회한다")
  void getMemberQuestionSets_whenFolderIdIsNull() {
    // given
    Long memberId = 1L;
    Long cursor = null;
    int size = 10;
    Long folderId = null;

    // when
    questionSetWithStatsFacade.getMemberQuestionSetsWithProgress(memberId, cursor, size, folderId);

    // then
    verify(questionSetPublicApi).getMemberQuestionSets(memberId, cursor, size);
  }

  @Test
  @DisplayName("folderId가 '전체' 폴더이면, 폴더 필터링 없이 모든 문제집을 조회한다")
  void getMemberQuestionSets_whenFolderIsAllScope() {
    // given
    Long memberId = 1L;
    Long cursor = null;
    int size = 10;
    Long folderId = 100L;
    CommonFolder allFolder =
        CommonFolder.create("전체", CommonFolderType.QUESTION_SET, FolderScope.ALL, 0, memberId);
    given(commonFolderPublicApi.findFolderEntityById(memberId, folderId))
        .willReturn(Optional.of(allFolder));

    // when
    questionSetWithStatsFacade.getMemberQuestionSetsWithProgress(memberId, cursor, size, folderId);

    // then
    verify(questionSetPublicApi).getMemberQuestionSets(memberId, cursor, size);
  }

  @Test
  @DisplayName("folderId가 '사용자 정의' 폴더이면, 해당 folderId로 문제집을 필터링하여 조회한다")
  void getMemberQuestionSets_whenFolderIsCustomScope() {
    // given
    Long memberId = 1L;
    Long cursor = null;
    int size = 10;
    Long folderId = 101L;
    CommonFolder customFolder =
        CommonFolder.create("내 폴더", CommonFolderType.QUESTION_SET, FolderScope.CUSTOM, 1, memberId);
    given(commonFolderPublicApi.findFolderEntityById(memberId, folderId))
        .willReturn(Optional.of(customFolder));

    // when
    questionSetWithStatsFacade.getMemberQuestionSetsWithProgress(memberId, cursor, size, folderId);

    // then
    verify(questionSetPublicApi).getMemberQuestionSets(memberId, cursor, size, folderId);
  }

  @Test
  @DisplayName("학습 통계를 올바르게 반환한다")
  void shouldReturnCorrectLearnStats() {
    // given
    Long memberId = 1L;
    LearnStats learnStats = LearnStats.newOf(memberId);
    ReflectionTestUtils.setField(learnStats, "totalSolvedQuestionSetCount", 10);
    ReflectionTestUtils.setField(learnStats, "totalQuestionCount", 100L);
    ReflectionTestUtils.setField(learnStats, "totalCorrectQuestionCount", 50L);

    given(learnStatsPublicApi.getLearnStats(memberId)).willReturn(Optional.of(learnStats));
    given(questionSetPublicApi.countByMemberId(memberId)).willReturn(20L);

    // when
    MyQuestionSetsWithStatsResponse response =
        questionSetWithStatsFacade.getMemberQuestionSetsWithProgress(memberId, null, 10, null);

    // then
    assertThat(response.learnStats().getTotalQuestionSetCount()).isEqualTo(20);
    assertThat(response.learnStats().getTotalSolvedQuestionSetCount()).isEqualTo(10);
    assertThat(response.learnStats().getTotalQuestionCount()).isEqualTo(100L);
    assertThat(response.learnStats().getTotalCorrectQuestionCount()).isEqualTo(50L);
  }

  @Test
  @DisplayName("학습 통계가 없을 경우, 기본 통계 정보를 반환한다")
  void shouldReturnDefaultLearnStatsWhenStatsIsEmpty() {
    // given
    Long memberId = 1L;
    given(learnStatsPublicApi.getLearnStats(memberId)).willReturn(Optional.empty());
    given(questionSetPublicApi.countByMemberId(memberId)).willReturn(0L);

    // when
    MyQuestionSetsWithStatsResponse response =
        questionSetWithStatsFacade.getMemberQuestionSetsWithProgress(memberId, null, 10, null);

    // then
    assertThat(response.learnStats().getTotalQuestionSetCount()).isZero();
    assertThat(response.learnStats().getTotalSolvedQuestionSetCount()).isZero();
    assertThat(response.learnStats().getTotalQuestionCount()).isZero();
    assertThat(response.learnStats().getTotalCorrectQuestionCount()).isZero();
    assertThat(response.learnStats().getConsecutiveLearningDays()).isZero();
    assertThat(response.learnStats().getLastLearningDate()).isNull();
  }
}
