package kr.it.pullit.modules.commonfolder.web.apidocs;

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

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "폴더 삭제",
    description =
        """
            인증된 사용자의 특정 폴더를 삭제합니다. 삭제된 폴더에 포함되어 있던 컨텐츠는 '전체' 폴더로 이동됩니다.

            [Request]
            - `id`: 삭제할 폴더 ID (Path Variable, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, `204 No Content`를 반환합니다.
            - '전체' 폴더는 삭제할 수 없습니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(responseCode = "204", description = "폴더 삭제 성공"),
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청 (예: '전체' 폴더 삭제 시도)",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples =
                  @ExampleObject(
                      name = "기본 폴더 삭제 시도",
                      value =
                          """
                                {
                                  "type": "about:blank",
                                  "title": "Bad Request",
                                  "status": 400,
                                  "detail": "기본 폴더는 삭제할 수 없습니다.",
                                  "instance": "/api/common-folders/1",
                                  "code": "CF_001"
                                }
                                """))),
  @ApiResponse(
      responseCode = "404",
      description = "존재하지 않거나 권한이 없는 폴더",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples =
                  @ExampleObject(
                      name = "폴더 조회 실패",
                      value =
                          """
                                {
                                  "type": "about:blank",
                                  "title": "Not Found",
                                  "status": 404,
                                  "detail": "해당 폴더를 찾을 수 없습니다.",
                                  "instance": "/api/common-folders/999",
                                  "code": "CF_003"
                                }
                                """)))
})
public @interface DeleteFolderApiDocs {}
