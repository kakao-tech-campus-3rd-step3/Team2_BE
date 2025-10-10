package kr.it.pullit.modules.questionset.client.dto.response;

import java.util.List;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record LlmGeneratedQuestionSetResponse(
    String title, List<LlmGeneratedQuestionResponse> questions) {}
