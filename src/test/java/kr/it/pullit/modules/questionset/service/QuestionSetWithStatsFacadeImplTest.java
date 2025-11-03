package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.entity.CommonFolder;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithProgressResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoUnitTest
@DisplayName("QuestionSetWithStatsFacadeImpl 단위 테스트")
class QuestionSetWithStatsFacadeImplTest {

  @Mock private QuestionSetPublicApi questionSetPublicApi;
  @Mock private LearnStatsPublicApi learnStatsPublicApi;
  @Mock private CommonFolderPublicApi commonFolderPublicApi;

  private QuestionSetWithStatsFacadeImpl facade;

  @BeforeEach
  void setUp() {
    facade =
        new QuestionSetWithStatsFacadeImpl(
            questionSetPublicApi, learnStatsPublicApi, commonFolderPublicApi);
  }

  @Nested
  @DisplayName("폴더 조건에 따른 조회")
  class DescribeFolderHandling {

    @Test
    @DisplayName("폴더 ID가 없으면 전체 폴더로 조회한다")
    void fetchesAllWhenFolderMissing() {
      Long memberId = 1L;
      CursorPageResponse<MyQuestionSetsResponse> pageResponse = cursorResponse();
      LearnStats learnStats = learnStatsWithSolvedCount(memberId, 1);

      when(questionSetPublicApi.getMemberQuestionSets(memberId, null, 5))
          .thenReturn(pageResponse);
      when(questionSetPublicApi.countByMemberId(memberId)).thenReturn(2L);
      when(learnStatsPublicApi.getLearnStats(memberId)).thenReturn(Optional.of(learnStats));

      MyQuestionSetsWithProgressResponse response =
          facade.getMemberQuestionSetsWithProgress(memberId, null, 5, null);

      assertThat(response.questionSets()).isEqualTo(pageResponse);
      assertThat(response.learningProgress()).isEqualTo(50);
      verify(questionSetPublicApi).getMemberQuestionSets(memberId, null, 5);
      verifyNoInteractions(commonFolderPublicApi);
    }

    @Test
    @DisplayName("폴더가 ALL 범위이면 전체 폴더와 동일하게 동작한다")
    void fetchesAllWhenFolderScopeAll() {
      Long memberId = 2L;
      Long folderId = 10L;
      CursorPageResponse<MyQuestionSetsResponse> pageResponse = cursorResponse();

      when(commonFolderPublicApi.findFolderEntityById(memberId, folderId))
          .thenReturn(Optional.of(allScopeFolder(folderId, memberId)));
      when(questionSetPublicApi.getMemberQuestionSets(memberId, null, 3))
          .thenReturn(pageResponse);
      when(questionSetPublicApi.countByMemberId(memberId)).thenReturn(0L);
      when(learnStatsPublicApi.getLearnStats(memberId)).thenReturn(Optional.empty());

      MyQuestionSetsWithProgressResponse response =
          facade.getMemberQuestionSetsWithProgress(memberId, null, 3, folderId);

      assertThat(response.questionSets()).isEqualTo(pageResponse);
      assertThat(response.learningProgress()).isZero();
      verify(questionSetPublicApi).getMemberQuestionSets(memberId, null, 3);
    }

    @Test
    @DisplayName("커스텀 폴더이면 폴더 기준으로 조회한다")
    void fetchesSpecificFolderWhenRequested() {
      Long memberId = 3L;
      Long folderId = 11L;
      CursorPageResponse<MyQuestionSetsResponse> pageResponse = cursorResponse();

      CommonFolder folder =
          CommonFolder.create("폴더", CommonFolderType.QUESTION_SET, FolderScope.CUSTOM, 0, memberId);
      ReflectionTestUtils.setField(folder, "id", folderId);

      when(commonFolderPublicApi.findFolderEntityById(memberId, folderId))
          .thenReturn(Optional.of(folder));
      when(questionSetPublicApi.getMemberQuestionSets(memberId, null, 4, folderId))
          .thenReturn(pageResponse);
      when(questionSetPublicApi.countByMemberId(memberId)).thenReturn(1L);
      when(learnStatsPublicApi.getLearnStats(memberId)).thenReturn(Optional.empty());

      MyQuestionSetsWithProgressResponse response =
          facade.getMemberQuestionSetsWithProgress(memberId, null, 4, folderId);

      assertThat(response.questionSets()).isEqualTo(pageResponse);
      verify(questionSetPublicApi).getMemberQuestionSets(memberId, null, 4, folderId);
    }
  }

  private CursorPageResponse<MyQuestionSetsResponse> cursorResponse() {
    MyQuestionSetsResponse item =
        MyQuestionSetsResponse.builder()
            .questionSetId(1L)
            .title("문제집")
            .sourceIds(List.of())
            .sourceNames(List.of())
            .questionCount(0)
            .difficultyType(null)
            .questionType(null)
            .status(null)
            .commonFolderId(null)
            .commonFolderName(null)
            .createdAt(null)
            .build();
    return CursorPageResponse.of(List.of(item), 5, MyQuestionSetsResponse::questionSetId);
  }

  private CommonFolder allScopeFolder(Long folderId, Long memberId) {
    CommonFolder folder =
        CommonFolder.create(CommonFolder.DEFAULT_NAME, CommonFolderType.QUESTION_SET, FolderScope.ALL, 0, memberId);
    ReflectionTestUtils.setField(folder, "id", folderId);
    return folder;
  }

  private LearnStats learnStatsWithSolvedCount(Long memberId, int solved) {
    LearnStats learnStats = LearnStats.newOf(memberId);
    ReflectionTestUtils.setField(learnStats, "totalSolvedQuestionSetCount", solved);
    return learnStats;
  }
}
