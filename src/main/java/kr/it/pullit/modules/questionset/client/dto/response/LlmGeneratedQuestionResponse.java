package kr.it.pullit.modules.questionset.client.dto.response;

import java.util.List;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record LlmGeneratedQuestionResponse(
    Integer id, String questionText, List<String> options, String answer, String explanation) {}
