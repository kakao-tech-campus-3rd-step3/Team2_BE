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
        """
            인증된 사용자가 업로드한 학습 소스 목록을 최신 순으로 조회합니다.
            현재는 페이지네이션을 지원하지 않으며, 모든 소스를 한 번에 반환합니다.

            [Request]
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, `SourceResponse` 객체 배열을 반환합니다.
            - 업로드한 소스가 없을 경우 빈 배열을 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "소스 목록 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = SourceResponse.class)))),
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
