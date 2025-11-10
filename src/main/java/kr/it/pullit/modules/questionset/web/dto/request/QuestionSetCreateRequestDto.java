package kr.it.pullit.modules.questionset.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kr.it.pullit.modules.questionset.enums.DifficultyType;
import kr.it.pullit.modules.questionset.enums.QuestionType;

public record QuestionSetCreateRequestDto(
    @Schema(description = "문제집 난이도", requiredMode = Schema.RequiredMode.REQUIRED, example = "EASY")
        @NotNull(message = "난이도는 필수입니다.")
        DifficultyType difficulty,
    @Schema(
            description = "생성할 문제 수 (최소 1, 최대 100)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "1",
            maximum = "100",
            example = "20")
        @Min(value = 1, message = "문제 수는 최소 1개 이상이어야 합니다.")
        @Max(value = 100, message = "문제 수는 최대 100개까지 가능합니다.")
        int questionCount,
    @Schema(
            description = "문제 유형",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "MULTIPLE_CHOICE")
        @NotNull(message = "문제 유형은 필수입니다.")
        QuestionType type,
    @Schema(description = "문제집 생성에 사용할 소스 ID 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "학습 소스는 최소 1개 이상 선택해야 합니다.")
        List<Long> sourceIds,
    @Schema(description = "문제집을 저장할 폴더 ID (미지정 시 기본 폴더)", example = "5") Long commonFolderId) {}
