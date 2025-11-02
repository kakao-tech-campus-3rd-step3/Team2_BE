package kr.it.pullit.modules.wronganswer.domain;

import static org.assertj.core.api.Assertions.assertThat;

import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.support.annotation.MockitoUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("WrongAnswer 엔티티 단위 테스트")
@MockitoUnitTest
class WrongAnswerTest {

  @Test
  @DisplayName("WrongAnswer.create는 멤버와 문제 정보를 저장하고 isReviewed를 기본값 false로 설정한다")
  void shouldCreateWrongAnswerWithDefaults() {
    Question question = Mockito.mock(Question.class);

    WrongAnswer wrongAnswer = WrongAnswer.create(7L, question);

    assertThat(wrongAnswer.getMemberId()).isEqualTo(7L);
    assertThat(wrongAnswer.getQuestion()).isEqualTo(question);
    assertThat(wrongAnswer.getIsReviewed()).isFalse();
  }

  @Test
  @DisplayName("markAsReviewed는 isReviewed 플래그를 true로 변경한다")
  void shouldMarkWrongAnswerAsReviewed() {
    Question question = Mockito.mock(Question.class);
    WrongAnswer wrongAnswer = WrongAnswer.create(9L, question);

    wrongAnswer.markAsReviewed();

    assertThat(wrongAnswer.getIsReviewed()).isTrue();
  }
}
