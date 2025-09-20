package kr.it.pullit.modules.questionset.domain.entity;

import java.util.List;
import java.util.Objects;

public record QuestionGenerationRequest(
    Long ownerId,
    Long questionSetId,
    List<Long> sourceIds,
    QuestionGenerationSpecification specification) {
  public QuestionGenerationRequest {
    Objects.requireNonNull(ownerId, "OwnerId is required");
    Objects.requireNonNull(questionSetId, "QuestionSet ID is required");
    Objects.requireNonNull(specification, "Specification is required");
    Objects.requireNonNull(sourceIds, "Source IDs are required");
  }
}
