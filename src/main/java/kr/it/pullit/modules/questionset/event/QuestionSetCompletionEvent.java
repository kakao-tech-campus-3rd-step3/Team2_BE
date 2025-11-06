package kr.it.pullit.modules.questionset.event;

public record QuestionSetCompletionEvent(Long questionSetId, Long ownerId) {}
