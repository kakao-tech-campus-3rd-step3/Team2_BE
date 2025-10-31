package kr.it.pullit.modules.questionset.web.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record QuestionSetUpdateRequestDto(String title, @NotNull Long commonFolderId) {}
