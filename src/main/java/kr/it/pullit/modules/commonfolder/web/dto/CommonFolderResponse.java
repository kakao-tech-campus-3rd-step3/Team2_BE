package kr.it.pullit.modules.commonfolder.web.dto;

import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;

public record CommonFolderResponse(Long id, String name, CommonFolderType type, int sortOrder) {
}
