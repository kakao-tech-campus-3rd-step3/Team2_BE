package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithProgressResponse;
import kr.it.pullit.support.annotation.MockitoUnitTest;

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
  @DisplayName("학습 진행률을 올바르게 계산한다 (맞힌 문제 5개 / 전체 10개 -> 50%)")
  void shouldCalculateLearningProgressCorrectly() {
    // given
    Long memberId = 1L;
    LearnStats learnStats = LearnStats.newOf(memberId);
    ReflectionTestUtils.setField(learnStats, "totalSolvedQuestionCount", 5L);
    ReflectionTestUtils.setField(learnStats, "totalQuestionCount", 10L);

    given(learnStatsPublicApi.getLearnStats(memberId)).willReturn(Optional.of(learnStats));

    // when
    MyQuestionSetsWithProgressResponse response =
        questionSetWithStatsFacade.getMemberQuestionSetsWithProgress(memberId, null, 10, null);

    // then
    assertThat(response.learningProgress()).isEqualTo(50);
  }

  @Test
  @DisplayName("전체 문제가 0개일 경우, 학습 진행률은 0%이다")
  void shouldReturnZeroProgressWhenTotalQuestionsIsZero() {
    // given
    Long memberId = 1L;
    LearnStats learnStats = LearnStats.newOf(memberId);
    ReflectionTestUtils.setField(learnStats, "totalSolvedQuestionCount", 0L);
    ReflectionTestUtils.setField(learnStats, "totalQuestionCount", 0L);

    given(learnStatsPublicApi.getLearnStats(memberId)).willReturn(Optional.of(learnStats));

    // when
    MyQuestionSetsWithProgressResponse response =
        questionSetWithStatsFacade.getMemberQuestionSetsWithProgress(memberId, null, 10, null);

    // then
    assertThat(response.learningProgress()).isZero();
  }
}
