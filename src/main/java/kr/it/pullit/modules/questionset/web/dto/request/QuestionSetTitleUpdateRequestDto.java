package kr.it.pullit.modules.questionset.web.dto.request;

import static kr.it.pullit.modules.questionset.domain.QuestionSetConstants.TITLE_MAX_LENGTH;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionSetTitleUpdateRequestDto(
    @NotBlank(message = "제목은 비워둘 수 없습니다.")
        @Size(max = TITLE_MAX_LENGTH, message = "제목은 최대 " + TITLE_MAX_LENGTH + "자까지 입력 가능합니다.")
        String title) {}
