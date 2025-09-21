package kr.it.pullit.modules.questionset.web.dto.request;

import java.util.List;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;

public record QuestionSetCreateRequestDto(
    String title,
    DifficultyType difficulty,
    int questionCount,
    QuestionType type,
    List<Long> sourceIds) {}
