package kr.it.pullit.modules.learningsource.source.event;

public record SourceExtractionFailureEvent(Long sourceId, Throwable cause) {}
