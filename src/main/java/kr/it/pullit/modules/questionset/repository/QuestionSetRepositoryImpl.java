package kr.it.pullit.modules.questionset.repository;

import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.QuestionSetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QuestionSetRepositoryImpl implements QuestionSetRepository {
  private final QuestionSetJpaRepository questionSetJpaRepository;

  public Optional<QuestionSet> findById(Long id) {
    return questionSetJpaRepository.findById(id);
  }
}
