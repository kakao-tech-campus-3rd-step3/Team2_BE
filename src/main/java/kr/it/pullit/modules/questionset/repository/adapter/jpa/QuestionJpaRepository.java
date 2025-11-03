package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import kr.it.pullit.modules.questionset.domain.entity.Question;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {

  long countByQuestionSet_OwnerId(Long ownerId);
}
