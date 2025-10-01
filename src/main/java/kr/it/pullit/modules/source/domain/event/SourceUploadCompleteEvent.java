package kr.it.pullit.modules.source.domain.event;

public record SourceUploadCompleteEvent(Long sourceId, String s3Url) {}
