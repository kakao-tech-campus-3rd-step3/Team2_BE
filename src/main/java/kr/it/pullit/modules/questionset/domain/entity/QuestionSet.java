package kr.it.pullit.modules.questionset.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.exception.SourceNotFoundException;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.questionset.domain.dto.QuestionSetCreateParam;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class QuestionSet extends BaseEntity {

  @OneToMany(mappedBy = "questionSet", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<Question> questions = new ArrayList<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private Member owner;

  @ManyToMany
  @JoinTable(
      name = "question_set_source",
      joinColumns = @JoinColumn(name = "question_set_id"),
      inverseJoinColumns = @JoinColumn(name = "source_id"))
  private Set<Source> sources = new HashSet<>();

  private String title;

  @Enumerated(EnumType.STRING)
  private DifficultyType difficulty;

  @Enumerated(EnumType.STRING)
  private QuestionType type;

  /* 문제 수 */
  @Setter private Integer questionLength;

  @Enumerated(EnumType.STRING)
  private QuestionSetStatus status;

  @Builder
  public QuestionSet(
      Member owner,
      Set<Source> sources,
      String title,
      DifficultyType difficulty,
      QuestionType type,
      Integer questionLength) {
    this.owner = owner;
    this.sources = sources != null ? sources : new HashSet<>();
    this.title = title;
    this.difficulty = difficulty;
    this.type = type;
    this.questionLength = questionLength;
    this.status = QuestionSetStatus.PENDING;
  }

  public static QuestionSet create(
      Member owner, List<Source> sources, QuestionSetCreateParam param) {
    validateSources(sources);

    String title = sources.getFirst().getOriginalName();
    Set<Source> sourceSet = new HashSet<>(sources);

    return QuestionSet.builder()
        .owner(owner)
        .sources(sourceSet)
        .title(title)
        .difficulty(param.difficulty())
        .type(param.type())
        .questionLength(param.questionCount())
        .build();
  }

  private static void validateSources(List<Source> sources) {
    if (sources == null || sources.isEmpty()) {
      throw SourceNotFoundException.withMessage("소스가 존재하지 않습니다.");
    }
  }

  public void addQuestion(Question question) {
    questions.add(question);
    question.setQuestionSet(this);
  }

  public void removeQuestion(Question question) {
    questions.remove(question);
    question.setQuestionSet(null);
  }

  public void addSource(Source source) {
    sources.add(source);
    source.getQuestionSets().add(this);
  }

  public void removeSource(Source source) {
    sources.remove(source);
    source.getQuestionSets().remove(this);
  }

  public void completeProcessing() {
    this.status = QuestionSetStatus.COMPLETE;
  }

  public void failProcessing() {
    this.status = QuestionSetStatus.FAILED;
  }

  public void updateTitle(String title) {
    this.title = title;
  }
}
