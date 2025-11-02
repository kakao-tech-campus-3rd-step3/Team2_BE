package kr.it.pullit.modules.questionset.service;

import kr.it.pullit.modules.commonfolder.api.CommonFolderPublicApi;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;
import kr.it.pullit.modules.projection.learnstats.api.LearnStatsPublicApi;
import kr.it.pullit.modules.projection.learnstats.domain.LearnStats;
import kr.it.pullit.modules.projection.learnstats.web.dto.LearnStatsResponse;
import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.api.QuestionSetWithStatsFacade;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsResponse;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithProgressResponse;
import kr.it.pullit.shared.paging.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionSetWithStatsFacadeImpl implements QuestionSetWithStatsFacade {

  private final QuestionSetPublicApi questionSetPublicApi;
  private final LearnStatsPublicApi learnStatsPublicApi;
  private final CommonFolderPublicApi commonFolderPublicApi;

  @Override
  public MyQuestionSetsWithProgressResponse getMemberQuestionSetsWithProgress(
      Long memberId, Long cursor, int size, Long folderId) {

    boolean isAllFolder =
        folderId == null
            || commonFolderPublicApi
                .findFolderEntityById(memberId, folderId)
                .map(folder -> folder.getScope() == FolderScope.ALL)
                .orElse(false);

    CursorPageResponse<MyQuestionSetsResponse> questionSets;
    if (isAllFolder) {
      questionSets = questionSetPublicApi.getMemberQuestionSets(memberId, cursor, size);
    } else {
      questionSets = questionSetPublicApi.getMemberQuestionSets(memberId, cursor, size, folderId);
    }
    long totalCount = questionSetPublicApi.countByMemberId(memberId);

    LearnStats learnStats =
        learnStatsPublicApi.getLearnStats(memberId).orElseGet(() -> LearnStats.newOf(memberId));

    LearnStatsResponse learnStatsResponse = LearnStatsResponse.of(learnStats, (int) totalCount);

    int learningProgress = learnStatsResponse.calculateLearningProgress(totalCount);

    return new MyQuestionSetsWithProgressResponse(questionSets, learningProgress);
  }
}
