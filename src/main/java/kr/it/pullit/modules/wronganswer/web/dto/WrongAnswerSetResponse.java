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
    int incorrectCount) {}
