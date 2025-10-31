package kr.it.pullit.modules.learningsource.source.web.apidocs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.ProblemDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;

// TODO: 상태코드 정상화
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "학습 소스 업로드 URL 발급",
    description =
        "학습 소스 파일을 업로드할 때 사용할 Pre-signed URL을 발급합니다.\n\n"
            + "[Request]\n"
            + "- 인증 토큰 필요 (Bearer)\n"
            + "- 파일 메타데이터 (이름, MIME 타입, 크기)를 전달합니다.",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "업로드 URL 발급 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = SourceUploadResponse.class),
              examples =
                  @ExampleObject(
                      name = "성공",
                      summary = "업로드 URL 발급 성공",
                      value =
                          """
                        {
                          \"uploadUrl\": \"https://s3.amazonaws.com/pullit/uploads/12345\",
                          \"filePath\": \"uploads/2024/03/uuid-source.pdf\",
                          \"originalName\": \"orientation.pdf\",
                          \"contentType\": \"application/pdf\",
                          \"fileSizeBytes\": 204800,
                          \"uploadId\": \"18c9c8be-e5d4-4c37-b0c4-ccf3c4fc4b65\"
                        }
                        """))),
  @ApiResponse(
      responseCode = "400",
      description = "요청 파라미터가 유효하지 않음",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples = {
                @ExampleObject(
                    name = "잘못된 요청",
                    summary = "필수 값 누락",
                    value =
                        """
                        {
                          "type": "about:blank",
                          "title": "Bad Request",
                          "status": 400,
                          "detail": "파일명은 필수입니다",
                          "code": "C_001"
                        }
                        """),
                @ExampleObject(
                    name = "인수 타입 불일치",
                    ref = "#/components/examples/argumentTypeMismatchExample")
              }))
})
public @interface GenerateUploadUrlApiDocs {}
