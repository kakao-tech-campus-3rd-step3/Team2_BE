package kr.it.pullit.modules.commonfolder.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record FolderDeleteWarningResponse(
    @Schema(description = "폴더에 포함된 문제집(또는 소스)의 개수", example = "5") long questionSetCount) {}
