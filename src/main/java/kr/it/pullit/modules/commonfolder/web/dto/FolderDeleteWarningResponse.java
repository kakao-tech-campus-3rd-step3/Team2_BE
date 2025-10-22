package kr.it.pullit.modules.commonfolder.web.dto;

import lombok.Builder;

@Builder
public record FolderDeleteWarningResponse(long questionSetCount) {}
