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
@Operation(summary = "문제집 생성", description = "새로운 문제집을 생성합니다.")
@ApiResponses({
  @ApiResponse(responseCode = "201", description = "생성 성공"),
  @ApiResponse(
      responseCode = "400",
      description = "입력값 오류 (예: 소스 파일이 아직 처리 중)",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface CreateQuestionSetApiDocs {}
