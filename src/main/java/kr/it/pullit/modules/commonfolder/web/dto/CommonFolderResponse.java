package kr.it.pullit.modules.commonfolder.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;

public record CommonFolderResponse(
    @Schema(description = "폴더 ID", example = "1") Long id,
    @Schema(description = "폴더 이름", example = "Java 기초") String name,
    @Schema(description = "폴더 종류", example = "QUESTION_SET") CommonFolderType type,
    @Schema(description = "폴더 범위", example = "ALL") FolderScope scope,
    @Schema(description = "정렬 순서", example = "1") int sortOrder) {}
