package kr.it.pullit.modules.questionset.repository;

import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.Question;

public interface QuestionRepository {

  Optional<Question> findById(Long id);

  Question save(Question question);
}
