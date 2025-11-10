package kr.it.pullit.modules.questionset.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record MarkQuestionsResponse(
    @Schema(description = "개별 문제 채점 결과 목록") List<MarkingResultDto> results,
    @Schema(description = "총 문제 수", example = "20") int totalQuestions,
    @Schema(description = "맞힌 문제 수", example = "15") int correctCount) {
  public static MarkQuestionsResponse of(
      List<MarkingResultDto> results, int totalQuestions, int correctCount) {
    return new MarkQuestionsResponse(results, totalQuestions, correctCount);
  }
}
