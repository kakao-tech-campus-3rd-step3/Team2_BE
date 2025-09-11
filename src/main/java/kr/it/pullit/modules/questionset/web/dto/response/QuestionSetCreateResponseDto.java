package kr.it.pullit.modules.questionset.web.dto.response;

import kr.it.pullit.modules.questionset.domain.enums.PublishStatus;

public record QuestionSetCreateResponseDto(PublishStatus status, String message) {}
