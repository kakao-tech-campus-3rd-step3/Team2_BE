package kr.it.pullit.modules.questionset.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.questionset.web.dto.response.MyQuestionSetsWithStatsResponse;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "나의 문제집 목록 조회 (통계 포함)",
    description =
        """
            인증된 사용자의 문제집 목록을 페이지네이션으로 조회합니다.
            각 문제집의 진행률과 같은 학습 통계 정보가 함께 제공됩니다.

            [Request]
            - `cursor`: 다음 페이지 조회를 위한 커서 ID (옵션)
            - `size`: 페이지당 문제집 개수 (기본값: 10)
            - `folderId`: 특정 폴더에 속한 문제집만 필터링 (옵션)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 문제집 목록과 다음 페이지 조회를 위한 커서 정보를 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponse(
    responseCode = "200",
    description = "조회 성공",
    content = @Content(schema = @Schema(implementation = MyQuestionSetsWithStatsResponse.class)))
public @interface GetMyQuestionSetsApiDocs {}
