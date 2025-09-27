package kr.it.pullit.modules.questionset.web.dto.response;

public record QuestionSetCreationCompleteResponse(
    boolean success, Long questionSetId, String message) {}
