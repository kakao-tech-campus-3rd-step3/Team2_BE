package kr.it.pullit.modules.questionset.repository;

import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.MarkingResultJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MarkingResultRepositoryImpl implements MarkingResultRepository {

  private final MarkingResultJpaRepository jpaRepository;

  @Override
  public MarkingResult save(MarkingResult markingResult) {
    return jpaRepository.save(markingResult);
  }
}
