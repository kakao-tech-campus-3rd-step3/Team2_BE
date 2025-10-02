package kr.it.pullit.modules.learningsource.source.event;

public record SourceUploadCompleteEvent(Long sourceId, String s3Url) {}
