package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;

public interface QuestionSetRepository {

  Optional<QuestionSet> findById(Long id);

  Optional<QuestionSet> findByIdAndMemberId(Long id, Long memberId);

  Optional<QuestionSet> findByIdWithQuestionsForSolve(Long id, Long memberId);

  Optional<QuestionSet> findByIdWithoutQuestions(Long id, Long memberId);

  List<QuestionSet> findByMemberId(Long memberId);

  QuestionSet save(QuestionSet questionSet);

  Optional<QuestionSet> findWrongAnswersByIdAndMemberId(Long questionSetId, Long memberId);
}
