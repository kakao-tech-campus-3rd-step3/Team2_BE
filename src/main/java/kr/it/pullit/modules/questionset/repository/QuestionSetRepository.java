package kr.it.pullit.modules.questionset.repository;

import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSetRepository extends JpaRepository<QuestionSet, Long> {}
