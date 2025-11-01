package kr.it.pullit.modules.questionset.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import kr.it.pullit.modules.questionset.domain.entity.MarkingResult;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.modules.questionset.repository.adapter.jpa.MarkingResultJpaRepository;
import kr.it.pullit.support.annotation.JpaSliceTest;
import kr.it.pullit.support.fixture.QuestionFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@JpaSliceTest
@Import({
  MarkingResultRepositoryImpl.class,
  QuestionSetRepositoryImpl.class,
  QuestionRepositoryImpl.class
})
@DisplayName("MarkingResultRepository 슬라이스 테스트")
class MarkingResultRepositoryTest {

  @Autowired private MarkingResultRepository repository;

  @Autowired private MarkingResultJpaRepository jpaRepository;

  @Autowired private QuestionSetRepository questionSetRepository;

  @Autowired private QuestionRepository questionRepository;

  @Nested
  @DisplayName("save 메서드는")
  class DescribeSave {

    @Test
    @DisplayName("MarkingResult를 저장하고 조회할 수 있다")
    void shouldSaveAndRetrieveMarkingResult() {
      // given
      Question question = createAndSaveTestQuestion();
      MarkingResult markingResult = MarkingResult.create(1L, question, true);

      // when
      MarkingResult saved = repository.save(markingResult);

      // then
      assertThat(saved.getId()).isNotNull();
      assertThat(saved.getMemberId()).isEqualTo(1L);
      assertThat(saved.isCorrect()).isTrue();
      assertThat(saved.getQuestion()).isNotNull();
      assertThat(saved.getQuestion().getId()).isEqualTo(question.getId());
    }
  }

  @Nested
  @DisplayName("조회 테스트")
  class DescribeFind {

    @Test
    @DisplayName("저장된 MarkingResult를 ID로 조회할 수 있다")
    void shouldFindById() {
      // given
      Question question = createAndSaveTestQuestion();
      MarkingResult saved = repository.save(MarkingResult.create(1L, question, true));

      // when
      Optional<MarkingResult> found = jpaRepository.findById(saved.getId());

      // then
      assertThat(found).isPresent();
      assertThat(found.get().getMemberId()).isEqualTo(1L);
      assertThat(found.get().isCorrect()).isTrue();
    }
  }

  private Question createAndSaveTestQuestion() {
    TrueFalseQuestion question = QuestionFixtures.aCorrectTrueFalseQuestion();
    questionSetRepository.save(question.getQuestionSet());
    return questionRepository.save(question);
  }
}
