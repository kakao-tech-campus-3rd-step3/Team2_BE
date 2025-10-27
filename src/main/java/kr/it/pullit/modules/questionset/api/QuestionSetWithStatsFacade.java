package kr.it.pullit.modules.questionset.api;

import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithProgressResponse;

public interface QuestionSetWithStatsFacade {
  MyQuestionSetsWithProgressResponse getMemberQuestionSetsWithProgress(
      Long memberId, Long cursor, int size, Long folderId);
}
