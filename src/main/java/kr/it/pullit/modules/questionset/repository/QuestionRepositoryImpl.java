package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.QuestionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepository {

  private final QuestionJpaRepository questionJpaRepository;

  @Override
  public Optional<Question> findById(Long id) {
    return questionJpaRepository.findById(id);
  }

  @Override
  public List<Question> findAllById(List<Long> ids) {
    return questionJpaRepository.findAllById(ids);
  }

  @Override
  public Question save(Question question) {
    return questionJpaRepository.save(question);
  }

  @Override
  public void deleteById(Long id) {
    questionJpaRepository.deleteById(id);
  }
}
