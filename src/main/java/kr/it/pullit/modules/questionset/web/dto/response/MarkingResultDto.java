package kr.it.pullit.modules.questionset.web.dto.response;

public record MarkingResultDto(Long questionId, boolean isCorrect) {

  public static MarkingResultDto of(Long questionId, boolean isCorrect) {
    return new MarkingResultDto(questionId, isCorrect);
  }
}
