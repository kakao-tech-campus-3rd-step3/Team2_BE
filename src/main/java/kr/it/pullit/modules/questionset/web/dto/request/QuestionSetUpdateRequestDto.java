package kr.it.pullit.modules.questionset.web.dto.request;

import lombok.Builder;

@Builder
public record QuestionSetUpdateRequestDto(String title, Long commonFolderId) {}
