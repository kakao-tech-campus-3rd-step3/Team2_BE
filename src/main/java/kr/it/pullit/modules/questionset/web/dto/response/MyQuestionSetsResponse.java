package kr.it.pullit.modules.questionset.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.LearningStatus;
import kr.it.pullit.modules.questionset.enums.QuestionSetStatus;
import kr.it.pullit.modules.questionset.enums.QuestionType;
import lombok.Builder;

@Builder
public record MyQuestionSetsResponse(
    @Schema(description = "문제집 ID", example = "1") Long questionSetId,
    @Schema(description = "문제집 제목", example = "Java 기초 문제집") String title,
    @Schema(description = "소스 ID 목록", example = "[1, 2]") List<Long> sourceIds,
    @Schema(description = "소스 이름 목록", example = "[\"Java.pdf\", \"객체지향.pdf\"]")
        List<String> sourceNames,
    @Schema(description = "문제 수", example = "20") Integer questionCount,
    @Schema(description = "난이도", example = "EASY") DifficultyType difficultyType,
    @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE") QuestionType questionType,
    @Schema(description = "문제집 생성 상태", example = "COMPLETED") QuestionSetStatus status,
    @Schema(description = "학습 상태", example = "IN_PROGRESS") LearningStatus learningStatus,
    @Schema(description = "폴더 ID", example = "5") Long commonFolderId,
    @Schema(description = "폴더 이름", example = "기본 폴더") String commonFolderName,
    @Schema(description = "생성 시간", example = "2025-01-01T12:00:00") LocalDateTime createdAt) {

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
        .learningStatus(questionSet.getLearningStatus())
        .commonFolderId(folderId)
        .commonFolderName(folderName)
        .createdAt(questionSet.getCreatedAt())
        .build();
  }
}
