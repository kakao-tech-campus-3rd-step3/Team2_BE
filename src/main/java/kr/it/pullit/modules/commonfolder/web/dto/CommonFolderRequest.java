package kr.it.pullit.modules.commonfolder.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CommonFolderRequest(@NotBlank(message = "폴더 이름은 필수입니다.") String name) {}
