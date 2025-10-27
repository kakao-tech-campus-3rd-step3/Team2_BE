package kr.it.pullit.modules.questionset.service;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.questionset.api.QuestionSetPublicApi;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.repository.QuestionSetRepository;
import kr.it.pullit.support.annotation.IntegrationTest;
import kr.it.pullit.support.builder.TestQuestionSetBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DisplayName("QuestionSetService 통합 테스트")
class QuestionSetServiceIntegrationTest {

  @Autowired private QuestionSetPublicApi publicApi;
  @Autowired private QuestionSetRepository repository;

  @Test
  @DisplayName("제목을 수정하면 DB에 반영된다")
  void updateTitle_persists() {
    // given
    Long ownerId = 101L;
    QuestionSet saved =
        repository.save(TestQuestionSetBuilder.builder().ownerId(ownerId).title("old").build());

    // when
    publicApi.updateTitle(saved.getId(), "new-title", ownerId);

    // then
    QuestionSet updated = repository.findById(saved.getId()).orElseThrow();
    assertThat(updated.getTitle()).isEqualTo("new-title");
  }

  @Test
  @DisplayName("삭제하면 조회되지 않는다")
  void delete_removesRow() {
    // given
    Long ownerId = 102L;
    QuestionSet saved =
        repository.save(
            TestQuestionSetBuilder.builder().ownerId(ownerId).title("to-delete").build());

    // when
    publicApi.delete(saved.getId(), ownerId);

    // then
    assertThat(repository.findById(saved.getId())).isEmpty();
  }
}
