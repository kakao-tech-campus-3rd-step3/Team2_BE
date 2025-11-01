package kr.it.pullit.modules.questionset.repository.adapter.jpa;

import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarkingResultJpaRepository extends JpaRepository<MarkingResult, Long> {}
