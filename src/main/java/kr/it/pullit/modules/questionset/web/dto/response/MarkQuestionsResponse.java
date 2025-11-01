package kr.it.pullit.modules.questionset.web.dto.response;

import java.util.List;

public record MarkQuestionsResponse(
    List<MarkingResultDto> results, int totalQuestions, int correctCount) {
  public static MarkQuestionsResponse of(
      List<MarkingResultDto> results, int totalQuestions, int correctCount) {
    return new MarkQuestionsResponse(results, totalQuestions, correctCount);
  }
}
