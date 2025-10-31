package kr.it.pullit.modules.commonfolder.web.dto;

import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;

public record QuestionSetFolderRequest(String name, CommonFolderType type) {}
