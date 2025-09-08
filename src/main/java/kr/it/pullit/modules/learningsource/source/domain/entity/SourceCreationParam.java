package kr.it.pullit.modules.learningsource.source.domain.entity;

public record SourceCreationParam(
    Long memberId, String originalName, String filePath, String contentType, Long fileSizeBytes) {}
