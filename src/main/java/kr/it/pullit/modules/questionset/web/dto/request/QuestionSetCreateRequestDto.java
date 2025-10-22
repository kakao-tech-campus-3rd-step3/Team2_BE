package kr.it.pullit.modules.questionset.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kr.it.pullit.modules.questionset.domain.enums.DifficultyType;
import kr.it.pullit.modules.questionset.domain.enums.QuestionType;

public record QuestionSetCreateRequestDto(
    @NotNull(message = "난이도는 필수입니다.") DifficultyType difficulty,
    @Min(value = 1, message = "문제 수는 최소 1개 이상이어야 합니다.")
        @Max(value = 100, message = "문제 수는 최대 100개까지 가능합니다.")
        int questionCount,
    @NotNull(message = "문제 유형은 필수입니다.") QuestionType type,
    @NotEmpty(message = "학습 소스는 최소 1개 이상 선택해야 합니다.") List<Long> sourceIds,
    Long commonFolderId) {}
