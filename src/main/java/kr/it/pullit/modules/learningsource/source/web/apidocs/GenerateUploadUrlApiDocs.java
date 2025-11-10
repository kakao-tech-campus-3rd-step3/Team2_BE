package kr.it.pullit.modules.learningsource.source.web.apidocs;

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

// TODO: 상태코드 정상화
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "S3 파일 업로드 URL 생성",
    description = "S3에 파일을 업로드하기 위한 Presigned URL을 생성합니다.")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "URL 생성 성공"),
  @ApiResponse(
      responseCode = "400",
      description = "파일 크기 제한 초과",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GenerateUploadUrlApiDocs {}
