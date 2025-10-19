package kr.it.pullit.modules.wronganswer.service.dto;

import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import lombok.Builder;

@Builder
public record WrongAnswerSetDto(QuestionSet questionSet, Long count, Long lastWrongAnswerId) {}
