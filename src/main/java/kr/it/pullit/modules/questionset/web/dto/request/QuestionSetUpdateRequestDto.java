package kr.it.pullit.modules.questionset.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record QuestionSetUpdateRequestDto(
    @Schema(description = "변경할 문제집 제목", example = "Java 심화 문제집") String title,
    @Schema(description = "변경할 폴더 ID", example = "10") Long commonFolderId) {}
