package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class QuestionSet extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ElementCollection private List<Long> sourceIds;
  private Long ownerId;
  private String title;
  private DifficultyType difficulty;
  private QuestionType type;
  /* 문제 수 */
  @Setter private Integer questionLength;

  @OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<Question> questions = new ArrayList<>();

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
