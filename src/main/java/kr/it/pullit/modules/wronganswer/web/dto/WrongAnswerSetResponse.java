package kr.it.pullit.modules.wronganswer.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

@Builder
public record WrongAnswerSetResponse(
    @Schema(description = "문제집 ID", example = "1") Long questionSetId,
    @Schema(description = "문제집 제목", example = "Java 기초 문제집") String questionSetTitle,
    @Schema(description = "소스 이름 목록", example = "[\"Java.pdf\", \"객체지향.pdf\"]")
        List<String> sourceNames,
    @Schema(description = "난이도", example = "EASY") DifficultyType difficulty,
    @Schema(description = "주요 토픽", example = "Java") String majorTopic,
    @Schema(description = "틀린 문제 수", example = "5") Long incorrectCount,
    @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE") QuestionType category,
    @JsonIgnore Long lastWrongAnswerId) {

  public static WrongAnswerSetResponse of(
      Long questionSetId,
      String questionSetTitle,
      List<String> sourceNames,
      DifficultyType difficulty,
      String majorTopic,
      Long incorrectCount,
      QuestionType category,
      Long lastWrongAnswerId) {
    return WrongAnswerSetResponse.builder()
        .questionSetId(questionSetId)
        .questionSetTitle(questionSetTitle)
        .sourceNames(sourceNames)
        .difficulty(difficulty)
        .majorTopic(majorTopic)
        .incorrectCount(incorrectCount)
        .category(category)
        .lastWrongAnswerId(lastWrongAnswerId)
        .build();
  }
}
