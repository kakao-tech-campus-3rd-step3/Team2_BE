package kr.it.pullit.modules.commonfolder.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;

public record CreateFolderRequest(@NotBlank String name, @NotNull CommonFolderType type) {}
