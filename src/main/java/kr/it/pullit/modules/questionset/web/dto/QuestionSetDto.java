package kr.it.pullit.modules.questionset.web.dto;

import java.time.LocalDateTime;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import lombok.Getter;

@Getter
public class QuestionSetDto {

  private final Long id;
  private final Long ownerID;
  private final String title;
  private final String difficulty;
  private final String type;
  private final Integer questionLength;
  private final LocalDateTime createTime;

  public QuestionSetDto(QuestionSet questionSet) {
    this.id = questionSet.getId();
    this.ownerID = questionSet.getOwnerId();
    this.title = questionSet.getTitle();
    this.difficulty = questionSet.getDifficulty();
    this.type = questionSet.getType();
    this.questionLength = questionSet.getQuestionLength();
    this.createTime = questionSet.getCreateTime();
  }
}
