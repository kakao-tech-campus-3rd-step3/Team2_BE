package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.Question;

public interface QuestionRepository {

  Optional<Question> findById(Long id);

  List<Question> findAllById(List<Long> ids);

  Question save(Question question);

  void deleteById(Long id);
}
