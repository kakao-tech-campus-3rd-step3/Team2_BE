package kr.it.pullit.modules.commonfolder.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;

public record UpdateFolderRequest(
    @Schema(description = "변경할 폴더의 이름", example = "Java 심화") String name,
    @Schema(description = "변경할 폴더의 종류", example = "QUESTION_SET") CommonFolderType type) {}
