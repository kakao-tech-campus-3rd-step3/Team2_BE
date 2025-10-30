package kr.it.pullit.modules.learningsource.source.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.ProblemDetail;

// TODO: 상태코드 정상화
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "학습 소스 업로드 완료 처리",
    description =
        "S3 업로드가 끝난 후 업로드 결과를 백엔드에 알립니다.\n\n"
            + "[Request]\n"
            + "- 인증 토큰 필요 (Bearer)\n"
            + "- 업로드 ID, 파일 경로 등의 정보를 전달합니다.",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "업로드 완료 처리 성공",
      content = @Content(schema = @Schema(hidden = true))),
  @ApiResponse(
      responseCode = "400",
      description = "업로드 완료 요청 정보가 유효하지 않음",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples =
                  @ExampleObject(
                      name = "검증 실패",
                      summary = "업로드 ID 누락",
                      value =
                          """
                        {
                          \"type\": \"about:blank\",
                          \"title\": \"Bad Request\",
                          \"status\": 400,
                          \"detail\": \"업로드 ID는 필수입니다\",
                          \"code\": \"C_001\"
                        }
                        """)))
})
public @interface ProcessUploadCompleteApiDocs {}
