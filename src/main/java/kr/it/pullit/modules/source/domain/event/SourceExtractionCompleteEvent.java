package kr.it.pullit.modules.source.domain.event;

import kr.it.pullit.modules.questionset.web.dto.request.QuestionSetCreateRequestDto;

public record SourceExtractionCompleteEvent(
    Long sourceId, QuestionSetCreateRequestDto questionRequest) {}
