package kr.it.pullit.modules.questionset.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.entity.MultipleChoiceQuestion;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.modules.questionset.domain.entity.ShortAnswerQuestion;
import kr.it.pullit.modules.questionset.domain.entity.TrueFalseQuestion;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.questionset.exception.InvalidQuestionException;
import kr.it.pullit.modules.questionset.exception.QuestionSetErrorCode;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionResponse(
    @Schema(description = "문제 ID", example = "101") Long id,
    @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE") QuestionType questionType,
    @Schema(description = "문제 내용", example = "다음 중 Java의 기본 타입이 아닌 것은?") String questionText,
    @Schema(
            description = "객관식 문제의 보기 (객관식 유형에만 존재)",
            example = "[\"int\", \"String\", \"boolean\", \"char\"]")
        List<String> options,
    @Schema(
            description = "정답 (객관식: 1, T/F: true, 주관식: \"정답\")",
            oneOf = {Integer.class, Boolean.class, String.class},
            example = "2")
        Object answer,
    @Schema(description = "해설", example = "String은 참조 타입입니다.") String explanation) {

  public static QuestionResponse from(Question question) {
    return createResponseByType(question);
  }

  private static QuestionResponse createResponseByType(Question question) {
    return switch (question.getQuestionType()) {
      case MULTIPLE_CHOICE -> fromMultipleChoiceQuestion((MultipleChoiceQuestion) question);
      case TRUE_FALSE -> fromTrueFalseQuestion((TrueFalseQuestion) question);
      case SHORT_ANSWER -> fromShortAnswerQuestion((ShortAnswerQuestion) question);
      case SUBJECTIVE ->
          throw new InvalidQuestionException(
              QuestionSetErrorCode.UNSUPPORTED_QUESTION_TYPE, question.getQuestionType());
    };
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
