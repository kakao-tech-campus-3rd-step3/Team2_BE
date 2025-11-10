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
    summary = "특정 폴더 상세 조회",
    description =
        """
            폴더 ID로 특정 폴더의 상세 정보를 조회합니다.
            '공통 폴더'가 아닌 사용자가 직접 생성한 폴더를 조회할 때 사용됩니다.

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
                      name = "사용자 폴더 조회 예시",
                      value =
                          """
                          {
                            "id": 101,
                            "name": "JPA 심화",
                            "type": "QUESTION_SET",
                            "scope": "CUSTOM",
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
