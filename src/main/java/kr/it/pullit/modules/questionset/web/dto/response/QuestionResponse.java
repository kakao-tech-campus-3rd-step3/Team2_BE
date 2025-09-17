package kr.it.pullit.modules.questionset.web.dto.response;

import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import lombok.Builder;

@Builder
public record QuestionResponse(
    Long id, String questionText, List<String> options, String answer, String explanation) {

  public static QuestionResponse from(Question question) {
    return QuestionResponse.builder()
        .id(question.getId())
        .questionText(question.getQuestionText())
        .options(question.getOptions())
        .answer(question.getAnswer())
        .explanation(question.getExplanation())
        .build();
  }
}
