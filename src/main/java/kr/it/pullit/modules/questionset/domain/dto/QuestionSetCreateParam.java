package kr.it.pullit.modules.questionset.domain.dto;

import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;

public record QuestionSetCreateParam(
    DifficultyType difficulty, int questionCount, QuestionType type) {

  public static QuestionSetCreateParam from(QuestionSetCreateRequestDto request) {
    return new QuestionSetCreateParam(
        request.difficulty(), request.questionCount(), request.type());
  }
}
