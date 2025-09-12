package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import kr.it.pullit.modules.questionset.domain.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {}
