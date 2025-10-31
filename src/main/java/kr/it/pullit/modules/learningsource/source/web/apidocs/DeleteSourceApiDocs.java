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
    summary = "학습 소스 삭제",
    description =
        "사용자가 업로드한 학습 소스를 삭제합니다.\n\n"
            + "[Request]\n"
            + "- 인증 토큰 필요 (Bearer)\n"
            + "- 경로 변수로 삭제할 소스 ID를 전달합니다.",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "소스 삭제 성공",
      content = @Content(schema = @Schema(hidden = true))),
  @ApiResponse(
      responseCode = "400",
      description = "소스 삭제 요청이 유효하지 않음",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples =
                  @ExampleObject(
                      name = "잘못된 요청",
                      summary = "삭제 권한 없음",
                      value =
                          """
                        {
                          \"type\": \"about:blank\",
                          \"title\": \"Bad Request\",
                          \"status\": 400,
                          \"detail\": \"요청한 소스를 삭제할 수 없습니다\",
                          \"code\": \"C_001\"
                        }
                        """)))
})
public @interface DeleteSourceApiDocs {}
