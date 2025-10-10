package kr.it.pullit.modules.questionset.web.dto.request;

public record MarkingServiceRequest(Long userId, Long questionId, Boolean isCorrect) {}
