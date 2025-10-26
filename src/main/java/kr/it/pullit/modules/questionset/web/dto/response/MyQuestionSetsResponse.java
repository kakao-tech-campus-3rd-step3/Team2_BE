package kr.it.pullit.modules.questionset.web.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

@Builder
public record MyQuestionSetsResponse(
    Long questionSetId,
    String title,
    List<Long> sourceIds,
    List<String> sourceNames,
    Integer questionCount,
    DifficultyType difficultyType,
    QuestionType questionType,
    QuestionSetStatus status,
    Long commonFolderId,
    String commonFolderName,
    LocalDateTime createdAt) {

  public static MyQuestionSetsResponse from(QuestionSet questionSet) {
    List<Long> sourceIds = questionSet.getSources().stream().map(Source::getId).toList();
    List<String> sourceNames =
        questionSet.getSources().stream().map(Source::getOriginalName).toList();
    Long folderId =
        questionSet.getCommonFolder() != null ? questionSet.getCommonFolder().getId() : null;
    String folderName =
        questionSet.getCommonFolder() != null ? questionSet.getCommonFolder().getName() : null;

    return MyQuestionSetsResponse.builder()
        .questionSetId(questionSet.getId())
        .title(questionSet.getTitle())
        .sourceIds(sourceIds)
        .sourceNames(sourceNames)
        .questionCount(questionSet.getQuestionLength())
        .difficultyType(questionSet.getDifficulty())
        .questionType(questionSet.getType())
        .status(questionSet.getStatus())
        .commonFolderId(folderId)
        .commonFolderName(folderName)
        .createdAt(questionSet.getCreatedAt())
        .build();
  }
}
