package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import kr.it.pullit.modules.questionset.domain.dto.QuestionUpdateParam;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import kr.it.pullit.modules.wronganswer.domain.entity.WrongAnswer;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "question")
@Getter
@NoArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@SQLDelete(sql = "UPDATE question SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public abstract class Question extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_set_id")
  private QuestionSet questionSet;

  @Column(columnDefinition = "TEXT")
  private String questionText;

  @Column(columnDefinition = "TEXT")
  private String explanation;

  @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  private WrongAnswer wrongAnswer;

  private LocalDateTime deletedAt;

  // 생성자는 SuperBuilder가 처리하므로 비워둡니다.

  void setQuestionSet(QuestionSet questionSet) {
    this.questionSet = questionSet;
  }

  // 자식 클래스에서 필드를 수정할 수 있도록 protected setter 제공
  protected void setQuestionText(String questionText) {
    this.questionText = questionText;
  }

  protected void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  // update 로직은 각 자식 클래스에서 구현해야 합니다.
  public abstract void update(QuestionUpdateParam param);

  public abstract boolean isCorrect(Object userAnswer);

  public abstract QuestionType getQuestionType();

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Question question)) {
      return false;
    }
    return id != null && id.equals(question.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
