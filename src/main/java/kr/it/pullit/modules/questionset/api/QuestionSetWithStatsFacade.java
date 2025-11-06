package kr.it.pullit.modules.questionset.api;

import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithStatsResponse;

public interface QuestionSetWithStatsFacade {
  MyQuestionSetsWithStatsResponse getMemberQuestionSetsWithProgress(
      Long memberId, Long cursor, int size, Long folderId);
}
