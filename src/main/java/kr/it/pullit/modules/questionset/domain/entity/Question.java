package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.List;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long sourceId;
  private Long questionSetId;
  private String questionText;
  private List<String> options;
  private String answer;
  private String explanation;

  public Question(
      Long sourceId,
      Long questionSetId,
      String questionText,
      List<String> options,
      String answer,
      String explanation) {
    this.sourceId = sourceId;
    this.questionSetId = questionSetId;
    this.questionText = questionText;
    this.options = options;
    this.answer = answer;
    this.explanation = explanation;
  }
}
