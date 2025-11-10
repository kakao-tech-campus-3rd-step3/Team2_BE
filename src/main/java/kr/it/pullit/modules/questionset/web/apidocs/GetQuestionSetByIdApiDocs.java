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
@Operation(summary = "문제집 상세 조회", description = "문제집 ID로 문제집의 모든 문제와 정보를 조회합니다. 오답노트 복습 모드를 지원합니다.")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "조회 성공"),
  @ApiResponse(
      responseCode = "404",
      description = "문제집이 없거나 접근 권한이 없는 경우",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GetQuestionSetByIdApiDocs {}
