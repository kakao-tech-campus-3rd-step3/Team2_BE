package kr.it.pullit.modules.questionset.web.dto.response;

public record QuestionSetCreationCompleteResponse(
    boolean success, Long questionSetId, String message) {
  public static QuestionSetCreationCompleteResponse of(
      boolean success, Long questionSetId, String message) {
    return new QuestionSetCreationCompleteResponse(success, questionSetId, message);
  }
}
