package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.IncorrectAnswerQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncorrectAnswerQuestionJpaRepository
    extends JpaRepository<IncorrectAnswerQuestion, Long> {
  Optional<IncorrectAnswerQuestion> findByMemberIdAndQuestionId(Long memberId, Long questionId);
}
