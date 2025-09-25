package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;

public interface QuestionSetRepository {

  Optional<QuestionSet> findById(Long id);

  Optional<QuestionSet> findByIdWithoutQuestions(Long id);

  List<QuestionSet> findByUserId(Long userId);

  QuestionSet save(QuestionSet questionSet);
}
