package kr.it.pullit.modules.questionset.web.dto.request;

import java.util.List;
import lombok.Builder;

@Builder
public record QuestionCreateRequest(
    Long questionSetId,
    String questionText,
    List<String> options,
    String answer,
    String explanation) {}
