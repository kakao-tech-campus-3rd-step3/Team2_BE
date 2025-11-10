package kr.it.pullit.modules.questionset.web.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MarkingRequest(
    @Schema(description = "문제 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
        @NotNull
        Long questionId,
    @Schema(
            description = "사용자가 제출한 답안 (문제 유형에 따라 타입 변경)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            oneOf = {Integer.class, Boolean.class, String.class},
            example = "2")
        @NotNull
        @JsonTypeInfo(use = Id.NAME, include = As.EXTERNAL_PROPERTY, property = "memberAnswerType")
        @JsonSubTypes({
          @JsonSubTypes.Type(value = Boolean.class, name = "boolean"),
          @JsonSubTypes.Type(value = String.class, name = "string")
        })
        Object memberAnswer) {

  public static MarkingRequest of(Long questionId, Object memberAnswer) {
    return new MarkingRequest(questionId, memberAnswer);
  }
}
