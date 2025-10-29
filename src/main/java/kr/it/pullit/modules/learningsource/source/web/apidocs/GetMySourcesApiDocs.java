package kr.it.pullit.modules.learningsource.source.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import kr.it.pullit.modules.learningsource.source.web.dto.SourceResponse;
import org.springframework.http.ProblemDetail;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "내 학습 소스 목록 조회",
    description =
        "사용자가 업로드한 학습 소스 목록을 최신 순으로 조회합니다.\n\n"
            + "[Request]\n"
            + "- 인증 토큰 필요 (Bearer)",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "소스 목록 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = SourceResponse.class)),
              examples =
                  @ExampleObject(
                      name = "성공",
                      summary = "소스 목록 조회",
                      value =
                          """
                                [
                                  {
                                    \"id\": 1,
                                    \"originalName\": \"orientation.pdf\",
                                    \"sourceFolderName\": \"기본 폴더\",
                                    \"status\": \"READY\",
                                    \"questionSetCount\": 2,
                                    \"pageCount\": 15,
                                    \"fileSizeBytes\": 204800,
                                    \"createdAt\": \"2024-03-01\",
                                    \"recentQuestionGeneratedAt\": \"2024-03-05\"
                                  }
                                ]
                                """))),
  @ApiResponse(
      responseCode = "400",
      description = "요청 정보가 유효하지 않음",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples =
                  @ExampleObject(
                      name = "잘못된 요청",
                      summary = "인증 실패",
                      value =
                          """
                                {
                                  \"type\": \"about:blank\",
                                  \"title\": \"Bad Request\",
                                  \"status\": 400,
                                  \"detail\": \"유효하지 않은 인증 정보입니다\",
                                  \"code\": \"C_001\"
                                }
                                """)))
})
public @interface GetMySourcesApiDocs {}
