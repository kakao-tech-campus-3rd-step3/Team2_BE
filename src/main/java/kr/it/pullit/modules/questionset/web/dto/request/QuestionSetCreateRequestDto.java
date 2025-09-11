package kr.it.pullit.modules.questionset.web.dto.request;

import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;

public record QuestionSetCreateRequestDto(
    DifficultyType difficulty, int questionCount, QuestionType type, String filePath) {}
