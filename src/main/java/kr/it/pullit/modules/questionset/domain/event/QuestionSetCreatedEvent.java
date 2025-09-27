package kr.it.pullit.modules.questionset.domain.event;

public record QuestionSetCreatedEvent(Long questionSetId, Long ownerId) {}
