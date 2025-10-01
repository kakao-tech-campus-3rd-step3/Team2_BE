package kr.it.pullit.modules.source.domain.event;

public record SourceExtractionFailureEvent(Long sourceId, Throwable cause) {}
