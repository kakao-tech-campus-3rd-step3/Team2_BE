package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.*;
import java.util.List;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** */
@Entity
@Getter
@NoArgsConstructor
public class Question extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long sourceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_set_id")
  private QuestionSet questionSet;

  private String questionText;

  @ElementCollection private List<String> options;

  private String answer;
  private String explanation;

  /**
   * Question 생성자
   *
   * @param sourceId 문제 출처 ID
   * @param questionSet 문제집
   * @param questionText 문제 제목
   * @param options 선지 목록 (오답만)
   * @param answer 정답
   * @param explanation 해설
   */
  public Question(
      Long sourceId,
      QuestionSet questionSet,
      String questionText,
      List<String> options,
      String answer,
      String explanation) {
    this.sourceId = sourceId;
    this.questionSet = questionSet;
    this.questionText = questionText;
    this.options = options;
    this.answer = answer;
    this.explanation = explanation;
  }
}
