package kr.it.pullit.modules.questionset.client.dto;

import java.util.List;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
public record LlmGeneratedQuestionDto(
    Integer id, String questionText, List<String> options, String answer, String explanation) {}
