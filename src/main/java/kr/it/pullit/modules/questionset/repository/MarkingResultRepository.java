package kr.it.pullit.modules.questionset.repository;

import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;

public interface MarkingResultRepository {
  MarkingResult save(MarkingResult markingResult);
}
