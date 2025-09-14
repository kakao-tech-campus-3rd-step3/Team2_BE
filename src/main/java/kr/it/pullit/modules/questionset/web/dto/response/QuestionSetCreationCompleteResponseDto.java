package kr.it.pullit.modules.questionset.web.dto.response;

public record QuestionSetCreationCompleteResponseDto(
    boolean success, Long questionSetId, String message) {}
