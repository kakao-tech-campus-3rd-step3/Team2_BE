package kr.it.pullit.modules.questionset.domain.event;

import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;

public record QuestionSetCreatedEvent(Long questionSetId, Long ownerId) {

  public static QuestionSetCreatedEvent from(QuestionSet questionSet) {
    return new QuestionSetCreatedEvent(questionSet.getId(), questionSet.getOwnerId());
  }
}
