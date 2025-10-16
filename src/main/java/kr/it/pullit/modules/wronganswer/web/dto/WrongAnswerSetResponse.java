package kr.it.pullit.modules.wronganswer.web.dto;

import java.util.List;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import lombok.Builder;

@Builder
public record WrongAnswerSetResponse(
    Long questionSetId,
    String questionSetTitle,
    List<String> sourceNames,
    DifficultyType difficulty,
    String majorTopic,
    Long incorrectCount,
    String category) {

  public static WrongAnswerSetResponse of(
      Long questionSetId,
      String questionSetTitle,
      List<String> sourceNames,
      DifficultyType difficulty,
      String majorTopic,
      Long incorrectCount,
      String category) {
    return WrongAnswerSetResponse.builder()
        .questionSetId(questionSetId)
        .questionSetTitle(questionSetTitle)
        .sourceNames(sourceNames)
        .difficulty(difficulty)
        .majorTopic(majorTopic)
        .incorrectCount(incorrectCount)
        .category(category)
        .build();
  }
}
