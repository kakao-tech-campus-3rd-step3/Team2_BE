package kr.it.pullit.modules.questionset.web.dto.response;

public record MarkingResult(Long questionId, boolean isCorrect) {

  public static MarkingResult of(Long questionId, boolean isCorrect) {
    return new MarkingResult(questionId, isCorrect);
  }
}
