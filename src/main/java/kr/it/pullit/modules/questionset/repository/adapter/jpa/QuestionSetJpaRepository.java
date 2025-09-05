package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSetJpaRepository extends JpaRepository<QuestionSet, Long> {}
