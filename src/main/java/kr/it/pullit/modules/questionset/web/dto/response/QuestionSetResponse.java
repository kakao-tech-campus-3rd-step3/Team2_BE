package kr.it.pullit.modules.questionset.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Getter;

@Getter
public class QuestionSetResponse {

  @Schema(description = "문제집 ID", example = "1")
  private final Long id;

  @Schema(description = "소스 ID 목록", example = "[1, 2]")
  private final List<Long> sourceIds;

  @Schema(description = "소유자 ID", example = "10")
  private final Long ownerID;

  @Schema(description = "문제집 제목", example = "Java 기초 문제집")
  private final String title;

  @Schema(description = "문제 목록")
  private final List<QuestionResponse> questions;

  @Schema(description = "난이도", example = "EASY")
  private final DifficultyType difficulty;

  @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE")
  private final QuestionType type;

  @Schema(description = "문제 수", example = "20")
  private final Integer questionLength;

  @Schema(description = "폴더 ID", example = "5")
  private final Long commonFolderId;

  @Schema(description = "폴더 이름", example = "기본 폴더")
  private final String commonFolderName;

  @Schema(description = "생성 시간", example = "2025-01-01T12:00:00")
  private final LocalDateTime createTime;

  @Schema(description = "수정 시간", example = "2025-01-02T15:30:00")
  private final LocalDateTime updateTime;

  public QuestionSetResponse(QuestionSet questionSet) {
    this.id = questionSet.getId();
    this.ownerID = questionSet.getOwnerId();
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
    if (questionSet.getCommonFolder() != null) {
      this.commonFolderId = questionSet.getCommonFolder().getId();
      this.commonFolderName = questionSet.getCommonFolder().getName();
    } else {
      this.commonFolderId = null;
      this.commonFolderName = null;
    }
  }

  public static QuestionSetResponse from(QuestionSet questionSet) {
    return new QuestionSetResponse(questionSet);
  }
}
