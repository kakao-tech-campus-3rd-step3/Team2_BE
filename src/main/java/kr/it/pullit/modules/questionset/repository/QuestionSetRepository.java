package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;

public interface QuestionSetRepository {

  Optional<QuestionSet> findById(Long id);

  Optional<QuestionSet> findByIdAndMemberId(Long id, Long memberId);

  Optional<QuestionSet> findWithQuestionsForFirstSolving(Long id, Long memberId);

  Optional<QuestionSet> findWithoutQuestions(Long id, Long memberId);

  List<QuestionSet> findByMemberId(Long memberId);

  List<QuestionSet> findAllByCommonFolderId(Long commonFolderId);

  long countByCommonFolderId(Long commonFolderId);

  List<QuestionSet> findByMemberIdAndFolderIdWithCursorAndNextPageCheck(
      Long memberId, Long folderId, Long cursor, int size);

  List<QuestionSet> findByMemberIdWithCursorAndNextPageCheck(Long memberId, Long cursor, int size);

  QuestionSet save(QuestionSet questionSet);

  void deleteById(Long questionSetId);

  void deleteAllByIds(List<Long> questionSetIds);

  Optional<QuestionSet> findQuestionSetForReviewing(Long questionSetId, Long memberId);

  Optional<QuestionSetResponse> findQuestionSetWhenHaveNoQuestionsYet(Long id, Long memberId);

  List<QuestionSet> findCompletedByMemberId(Long memberId);

  long countByOwnerId(Long memberId);
}
