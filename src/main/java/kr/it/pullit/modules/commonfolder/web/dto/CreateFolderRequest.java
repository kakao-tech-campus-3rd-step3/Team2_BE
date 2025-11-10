package kr.it.pullit.modules.commonfolder.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;

public record CreateFolderRequest(
    @Schema(
            description = "생성할 폴더의 이름",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Java 기초")
        @NotBlank
        String name,
    @Schema(
            description = "폴더의 종류 (문제집 또는 소스)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "QUESTION_SET")
        @NotNull
        CommonFolderType type) {}
