package kr.it.pullit.modules.wronganswer.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import lombok.Builder;

@Builder
public record WrongAnswerSetResponse(
    Long questionSetId,
    String questionSetTitle,
    List<String> sourceNames,
    DifficultyType difficulty,
    String majorTopic,
    Long incorrectCount,
    String category,
    @JsonIgnore Long lastWrongAnswerId) {

  public static WrongAnswerSetResponse of(
      Long questionSetId,
      String questionSetTitle,
      List<String> sourceNames,
      DifficultyType difficulty,
      String majorTopic,
      Long incorrectCount,
      String category,
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
