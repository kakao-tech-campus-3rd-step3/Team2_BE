package kr.it.pullit.modules.questionset.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record QuestionSetFolderUpdateRequestDto(
    @NotNull(message = "폴더 ID는 필수입니다.") Long commonFolderId) {}
