package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "marking_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarkingResult extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(nullable = false)
  private boolean isCorrect;

  @Builder(access = AccessLevel.PRIVATE)
  private MarkingResult(Long memberId, Question question, boolean isCorrect) {
    if (memberId == null) {
      throw new IllegalArgumentException("memberId는 null일 수 없습니다.");
    }
    if (question == null) {
      throw new IllegalArgumentException("question은 null일 수 없습니다.");
    }
    this.memberId = memberId;
    this.question = question;
    this.isCorrect = isCorrect;
  }

  public static MarkingResult create(Long memberId, Question question, boolean isCorrect) {
    return MarkingResult.builder()
        .memberId(memberId)
        .question(question)
        .isCorrect(isCorrect)
        .build();
  }
}
