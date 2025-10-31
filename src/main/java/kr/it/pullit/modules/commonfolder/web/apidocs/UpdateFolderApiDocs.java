package kr.it.pullit.modules.commonfolder.web.apidocs;

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
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;
import org.springframework.http.ProblemDetail;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "폴더 정보 수정",
    description =
        """
            인증된 사용자의 특정 폴더 정보를 수정합니다. (현재는 이름 변경만 지원)

            [Request]
            - `id`: 수정할 폴더 ID (Path Variable, 필수)
            - `name`: 변경할 새 폴더 이름 (Body, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 수정된 폴더의 상세 정보를 반환합니다.
            - '전체' 폴더는 수정할 수 없습니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "폴더 정보 수정 성공",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CommonFolderResponse.class),
              examples =
                  @ExampleObject(
                      name = "폴더 수정 응답",
                      value =
                          """
                          {
                            "id": 2,
                            "name": "Spring",
                            "type": "QUESTION_SET",
                            "sortOrder": 1
                          }
                          """))),
  @ApiResponse(
      responseCode = "400",
      description = "잘못된 요청 (예: '전체' 폴더 수정 시도)",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ProblemDetail.class),
              examples =
                  @ExampleObject(
                      name = "기본 폴더 수정 시도",
                      value =
                          """
                                {
                                  "type": "about:blank",
                                  "title": "Bad Request",
                                  "status": 400,
                                  "detail": "기본 폴더명은 변경할 수 없습니다.",
                                  "instance": "/api/common-folders/1",
                                  "code": "CF_002"
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
public @interface UpdateFolderApiDocs {}
