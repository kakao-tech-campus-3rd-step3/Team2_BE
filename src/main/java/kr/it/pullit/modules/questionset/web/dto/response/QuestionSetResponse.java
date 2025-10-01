package kr.it.pullit.modules.questionset.web.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;
import lombok.Getter;

@Getter
public class QuestionSetResponse {

  private final Long id;
  private final List<Long> sourceIds;
  private final Long ownerID;
  private final String title;
  private final List<QuestionResponse> questions;
  private final DifficultyType difficulty;
  private final QuestionType type;
  private final Integer questionLength;
  private final LocalDateTime createTime;
  private final LocalDateTime updateTime;

  public QuestionSetResponse(QuestionSet questionSet) {
    this.id = questionSet.getId();
    this.ownerID = questionSet.getOwner().getId();
    this.title = questionSet.getTitle();
    this.difficulty = questionSet.getDifficulty();
    this.type = questionSet.getType();
    this.questionLength = questionSet.getQuestionLength();
    this.createTime = questionSet.getCreatedAt();
    this.updateTime = questionSet.getUpdatedAt();
    this.questions =
        questionSet.getQuestions().stream()
            .map(QuestionResponse::from)
            .collect(Collectors.toList());
    this.sourceIds =
        questionSet.getSources().stream().map(Source::getId).collect(Collectors.toList());
  }

  private QuestionSetResponse(
      Long id,
      List<Long> sourceIds,
      Long ownerID,
      String title,
      List<QuestionResponse> questions,
      DifficultyType difficulty,
      QuestionType type,
      Integer questionLength,
      LocalDateTime createTime,
      LocalDateTime updateTime) {
    this.id = id;
    this.sourceIds = sourceIds;
    this.ownerID = ownerID;
    this.title = title;
    this.questions = questions;
    this.difficulty = difficulty;
    this.type = type;
    this.questionLength = questionLength;
    this.createTime = createTime;
    this.updateTime = updateTime;
  }
}
