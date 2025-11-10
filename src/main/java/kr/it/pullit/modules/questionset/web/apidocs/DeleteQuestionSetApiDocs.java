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
import org.springframework.http.ProblemDetail;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "문제집 삭제",
    description =
        """
            문제집 ID로 특정 문제집을 삭제합니다.

            [Request]
            - `id`: 삭제할 문제집 ID (Path Variable, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, `204 No Content`를 반환합니다.""")
@ApiResponses({
  @ApiResponse(
      responseCode = "204",
      description = "삭제 성공",
      content = @Content(schema = @Schema(hidden = true))),
  @ApiResponse(
      responseCode = "404",
      description = "문제집이 없거나 접근 권한이 없는 경우",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface DeleteQuestionSetApiDocs {}
