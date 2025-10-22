package kr.it.pullit.modules.commonfolder.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;

public record QuestionSetFolderRequest(
    @NotBlank(message = "폴더 이름은 필수입니다.") String name,
    @NotNull(message = "폴더 타입은 필수입니다.") CommonFolderType type) {}
