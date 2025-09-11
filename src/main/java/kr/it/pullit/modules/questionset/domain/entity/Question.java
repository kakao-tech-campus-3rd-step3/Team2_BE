package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.NoArgsConstructor;

/** */
@Entity
@NoArgsConstructor
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long sourceId;
  private Long questionSetId;
  private String questionText;
  @ElementCollection private List<String> options;
  private String answer;
  private String explanation;

  /**
   * Question 생성자
   *
   * @param sourceId 문제 출처 ID
   * @param questionSetId 문제집 ID
   * @param questionText 문제 제목
   * @param options 선지 목록 (오답만)
   * @param answer 정답
   * @param explanation 해설
   */
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
