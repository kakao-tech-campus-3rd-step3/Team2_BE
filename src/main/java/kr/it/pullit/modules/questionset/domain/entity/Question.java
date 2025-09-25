package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_set_id")
  private QuestionSet questionSet;

  @Column(columnDefinition = "TEXT")
  private String questionText;

  @ElementCollection private List<String> options;

  private String answer;

  @Column(columnDefinition = "TEXT")
  private String explanation;

  @OneToOne(mappedBy = "question")
  private IncorrectAnswerQuestion incorrectAnswerQuestion;

  /**
   * Question 생성자
   *
   * @param questionSet 문제집
   * @param questionText 문제 제목
   * @param options 선지 목록 (오답만)
   * @param answer 정답
   * @param explanation 해설
   */
  public Question(
      QuestionSet questionSet,
      String questionText,
      List<String> options,
      String answer,
      String explanation) {
    this.questionSet = questionSet;
    this.questionText = questionText;
    this.options = options;
    this.answer = answer;
    this.explanation = explanation;
  }

  void setQuestionSet(QuestionSet questionSet) {
    this.questionSet = questionSet;
  }

  /**
   * Question 수정
   *
   * @param questionText 문제 제목
   * @param options 선지 목록
   * @param answer 정답
   * @param explanation 해설
   */
  public void update(String questionText, List<String> options, String answer, String explanation) {
    this.questionText = questionText;
    this.options = options;
    this.answer = answer;
    this.explanation = explanation;
  }
}
