package kr.it.pullit.modules.questionset.web.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import jakarta.validation.constraints.NotNull;

public record MarkingRequest(
    @NotNull Long questionId,
    @NotNull
        @JsonTypeInfo(use = Id.NAME, include = As.EXTERNAL_PROPERTY, property = "memberAnswerType")
        @JsonSubTypes({
          @JsonSubTypes.Type(value = Boolean.class, name = "boolean"),
          @JsonSubTypes.Type(value = String.class, name = "string")
        })
        Object memberAnswer) {}
