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
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "특정 폴더 상세 조회",
    description =
        """
            인증된 사용자의 특정 폴더 ID에 해당하는 상세 정보를 조회합니다.

            [Request]
            - `id`: 폴더 ID (Path Variable, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 해당 폴더의 상세 정보를 반환합니다.
            - 자신의 폴더가 아니거나 존재하지 않는 폴더일 경우 404 Not Found를 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "폴더 상세 정보 조회 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CommonFolderResponse.class),
              examples =
                  @ExampleObject(
                      name = "폴더 상세 정보 응답",
                      value =
                          """
                          {
                            "id": 2,
                            "name": "JPA",
                            "type": "QUESTION_SET",
                            "sortOrder": 1
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
public @interface GetFolderByIdApiDocs {}
