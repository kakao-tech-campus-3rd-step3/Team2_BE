package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.IncorrectAnswerQuestion;

public interface IncorrectAnswerQuestionRepository {
  IncorrectAnswerQuestion save(IncorrectAnswerQuestion incorrectAnswerQuestion);

  List<IncorrectAnswerQuestion> saveAll(Iterable<IncorrectAnswerQuestion> entities);

  Optional<IncorrectAnswerQuestion> findByMemberIdAndQuestionId(Long memberId, Long questionId);
}
