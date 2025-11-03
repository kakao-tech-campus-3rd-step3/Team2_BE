package kr.it.pullit.modules.questionset.web.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kr.it.pullit.modules.questionset.domain.dto.QuestionUpdateParam;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.exception.InvalidQuestionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("QuestionResponse 변환 테스트")
class QuestionResponseTest {

  @Test
  @DisplayName("객관식 문제를 변환한다")
  void convertsMultipleChoiceQuestion() {
    MultipleChoiceQuestion question =
        MultipleChoiceQuestion.builder()
            .questionText("질문")
            .options(List.of("1", "2"))
            .answer("1")
            .explanation("해설")
            .build();

    QuestionResponse response = QuestionResponse.from(question);

    assertThat(response.questionType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
    assertThat(response.options()).containsExactly("1", "2");
  }

  @Test
  @DisplayName("OX 문제를 변환한다")
  void convertsTrueFalseQuestion() {
    TrueFalseQuestion question =
        TrueFalseQuestion.builder().questionText("질문").answer(true).explanation("해설").build();

    QuestionResponse response = QuestionResponse.from(question);

    assertThat(response.questionType()).isEqualTo(QuestionType.TRUE_FALSE);
    assertThat(response.answer()).isEqualTo(true);
  }

  @Test
  @DisplayName("단답형 문제를 변환한다")
  void convertsShortAnswerQuestion() {
    ShortAnswerQuestion question =
        ShortAnswerQuestion.builder().questionText("질문").answer("답").explanation("해설").build();

    QuestionResponse response = QuestionResponse.from(question);

    assertThat(response.questionType()).isEqualTo(QuestionType.SHORT_ANSWER);
    assertThat(response.answer()).isEqualTo("답");
  }

  @Test
  @DisplayName("지원하지 않는 문제 유형이면 예외를 던진다")
  void throwsWhenUnsupportedType() {
    Question unsupported = new UnsupportedQuestion();

    assertThatThrownBy(() -> QuestionResponse.from(unsupported))
        .isInstanceOf(InvalidQuestionException.class);
  }

  private static class UnsupportedQuestion extends Question {

    UnsupportedQuestion() {
      setQuestionText("질문");
      setExplanation("해설");
    }

    @Override
    public void update(QuestionUpdateParam param) {}

    @Override
    public boolean isCorrect(Object userAnswer) {
      return false;
    }

    @Override
    public QuestionType getQuestionType() {
      return QuestionType.SUBJECTIVE;
    }
  }
}
