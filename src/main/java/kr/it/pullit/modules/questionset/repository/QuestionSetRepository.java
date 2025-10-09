package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;

public interface QuestionSetRepository {

  Optional<QuestionSet> findById(Long id);

  Optional<QuestionSet> findByIdAndMemberId(Long id, Long memberId);

  Optional<QuestionSet> findByIdWithQuestionsForFirstSolving(Long id, Long memberId);

  Optional<QuestionSet> findByIdWithoutQuestions(Long id, Long memberId);

  List<QuestionSet> findByMemberId(Long memberId);

  QuestionSet save(QuestionSet questionSet);

  void delete(QuestionSet questionSet);

  Optional<QuestionSet> findQuestionSetForReviewing(Long questionSetId, Long memberId);

  Optional<QuestionSetResponse> findQuestionSetWhenHaveNoQuestionsYet(Long id, Long memberId);
}
