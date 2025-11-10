package kr.it.pullit.modules.questionset.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetResponse;
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "문제집 상세 조회",
    description =
        """
            문제집 ID로 문제집의 모든 문제와 상세 정보를 조회합니다.
            오답노트 복습 모드를 지원하여, 틀린 문제만 다시 풀어볼 수 있는 기능을 제공합니다.

            [Request]
            - `id`: 조회할 문제집 ID (Path Variable, 필수)
            - `isReviewing`: 오답노트 복습 모드 여부 (Query Param, 옵션, 기본값: false)
              - `true`로 설정 시, 해당 문제집에서 틀렸던 문제들만 조회합니다.
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 문제집의 상세 정보와 포함된 문제 목록을 반환합니다.""")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "조회 성공",
      content = @Content(schema = @Schema(implementation = QuestionSetResponse.class))),
  @ApiResponse(
      responseCode = "404",
      description = "문제집이 없거나 접근 권한이 없는 경우",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GetQuestionSetByIdApiDocs {}
