package kr.it.pullit.modules.questionset.repository;

import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.IncorrectAnswerQuestion;

public interface IncorrectAnswerQuestionRepository {
  IncorrectAnswerQuestion save(IncorrectAnswerQuestion incorrectAnswerQuestion);

  Optional<IncorrectAnswerQuestion> findByMemberIdAndQuestionId(Long memberId, Long questionId);
}
