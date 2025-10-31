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
import kr.it.pullit.modules.commonfolder.web.dto.FolderDeleteWarningResponse;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "폴더 삭제 경고 조회",
    description =
        """
            폴더를 삭제하기 전, 해당 폴더에 포함된 컨텐츠(예: 문제집)의 개수를 조회하여 사용자에게 경고를 표시하기 위한 API입니다.

            [Request]
            - `id`: 확인할 폴더 ID (Path Variable, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 해당 폴더에 포함된 컨텐츠의 개수를 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "컨텐츠 개수 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = FolderDeleteWarningResponse.class),
              examples =
                  @ExampleObject(
                      name = "삭제 경고 응답",
                      value =
                          """
                          {
                            "count": 5
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
                                  "instance": "/api/common-folders/999/delete-warning",
                                  "code": "CF_003"
                                }
                                """)))
})
public @interface GetFolderDeleteWarningApiDocs {}
