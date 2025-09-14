package kr.it.pullit.modules.questionset.web.dto.response;

public record QuestionCreationCompleteResponseDto(
    boolean success, Long questionSetId, String message) {}
