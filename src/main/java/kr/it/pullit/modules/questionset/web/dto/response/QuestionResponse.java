package kr.it.pullit.modules.questionset.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionResponse(
    Long id,
    QuestionType questionType,
    String questionText,
    List<String> options,
    Object answer,
    String explanation) {

  public static QuestionResponse from(Question question) {
    return createResponseByType(question);
  }

  private static QuestionResponse createResponseByType(Question question) {
    if (question instanceof MultipleChoiceQuestion mcq) {
      return fromMultipleChoiceQuestion(mcq);
    }

    if (question instanceof TrueFalseQuestion tfq) {
      return fromTrueFalseQuestion(tfq);
    }

    if (question instanceof ShortAnswerQuestion saq) {
      return fromShortAnswerQuestion(saq);
    }

    // TODO: 추후 예외 처리.
    throw new IllegalStateException(
        "Unknown question type: " + question.getClass().getSimpleName());
  }

  private static QuestionResponse fromMultipleChoiceQuestion(MultipleChoiceQuestion mcq) {
    return QuestionResponse.builder()
        .id(mcq.getId())
        .questionType(QuestionType.MULTIPLE_CHOICE)
        .questionText(mcq.getQuestionText())
        .options(mcq.getOptions())
        .answer(mcq.getAnswer())
        .explanation(mcq.getExplanation())
        .build();
  }

  private static QuestionResponse fromTrueFalseQuestion(TrueFalseQuestion tfq) {
    return QuestionResponse.builder()
        .id(tfq.getId())
        .questionType(QuestionType.TRUE_FALSE)
        .questionText(tfq.getQuestionText())
        .answer(tfq.isAnswer())
        .explanation(tfq.getExplanation())
        .build();
  }

  private static QuestionResponse fromShortAnswerQuestion(ShortAnswerQuestion saq) {
    return QuestionResponse.builder()
        .id(saq.getId())
        .questionType(QuestionType.SHORT_ANSWER)
        .questionText(saq.getQuestionText())
        .answer(saq.getAnswer())
        .explanation(saq.getExplanation())
        .build();
  }
}
