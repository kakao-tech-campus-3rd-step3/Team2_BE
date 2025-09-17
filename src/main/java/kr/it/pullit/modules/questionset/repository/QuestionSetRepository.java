package kr.it.pullit.modules.questionset.repository;

import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;

public interface QuestionSetRepository {

  Optional<QuestionSet> findById(Long id);

  QuestionSet save(QuestionSet questionSet);
}
