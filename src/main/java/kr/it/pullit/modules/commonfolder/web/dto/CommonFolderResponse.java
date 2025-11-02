package kr.it.pullit.modules.commonfolder.web.dto;

import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import kr.it.pullit.modules.commonfolder.domain.enums.FolderScope;

public record CommonFolderResponse(
    Long id, String name, CommonFolderType type, FolderScope scope, int sortOrder) {}
