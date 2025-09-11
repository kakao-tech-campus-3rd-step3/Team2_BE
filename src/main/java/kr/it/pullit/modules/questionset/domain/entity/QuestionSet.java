package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class QuestionSet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ElementCollection
  private List<Long> sourceIds;
  private Long ownerId;
  private String title;
  private DifficultyType difficulty;
  private QuestionType type;
  /* 문제 수 */
  @Setter private Integer questionLength;
  @CreatedDate private LocalDateTime createTime;

  public QuestionSet(
      Long ownerId,
      List<Long> sourceIds,
      String title,
      DifficultyType difficulty,
      QuestionType type,
      Integer questionLength) {
    this.ownerId = ownerId;
    this.sourceIds = sourceIds;
    this.title = title;
    this.difficulty = difficulty;
    this.type = type;
    this.questionLength = questionLength;
  }
}
