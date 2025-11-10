package kr.it.pullit.modules.questionset.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MarkingResultDto(
    @Schema(description = "문제 ID", example = "101") Long questionId,
    @Schema(description = "정답 여부", example = "true") boolean isCorrect) {

  public static MarkingResultDto of(Long questionId, boolean isCorrect) {
    return new MarkingResultDto(questionId, isCorrect);
  }
}
