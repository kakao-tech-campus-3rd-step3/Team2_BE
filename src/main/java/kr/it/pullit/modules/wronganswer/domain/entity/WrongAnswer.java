package kr.it.pullit.modules.wronganswer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kr.it.pullit.modules.questionset.domain.entity.Question;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wrong_answer")
@Getter
@NoArgsConstructor
public class WrongAnswer extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id")
  private Long memberId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id")
  private Question question;

  @Column private Boolean isReviewed;

  private WrongAnswer(Long memberId, Question question) {
    this.memberId = memberId;
    this.question = question;
    this.isReviewed = false;
  }

  public static WrongAnswer create(Long memberId, Question question) {
    return new WrongAnswer(memberId, question);
  }

  public void markAsReviewed() {
    this.isReviewed = true;
  }
}
