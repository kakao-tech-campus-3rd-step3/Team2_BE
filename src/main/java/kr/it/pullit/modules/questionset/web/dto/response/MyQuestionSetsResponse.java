package kr.it.pullit.modules.questionset.web.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import lombok.Builder;

@Builder
public record MyQuestionSetsResponse(
    Long questionSetId,
    String title,
    List<Long> sourceIds,
    List<String> sourceNames,
    Integer questionCount,
    DifficultyType difficultyType,
    QuestionType questionType,
    LocalDateTime createdAt) {}
