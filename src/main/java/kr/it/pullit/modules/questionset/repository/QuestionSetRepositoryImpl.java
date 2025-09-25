package kr.it.pullit.modules.questionset.repository;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.QuestionSetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class QuestionSetRepositoryImpl implements QuestionSetRepository {

  private final QuestionSetJpaRepository questionSetJpaRepository;

  @Override
  public Optional<QuestionSet> findById(Long id) {
    return questionSetJpaRepository.findByIdWithQuestions(id);
  }

  @Override
  public Optional<QuestionSet> findByIdWithoutQuestions(Long id) {
    return questionSetJpaRepository.findById(id);
  }

  @Override
  public QuestionSet save(QuestionSet questionSet) {
    return questionSetJpaRepository.save(questionSet);
  }

  @Override
  public List<QuestionSet> findByUserId(Long userId) {
    return questionSetJpaRepository.findByUserId(userId);
  }
}
