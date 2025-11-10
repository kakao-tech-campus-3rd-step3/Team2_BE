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
                      name = "폴더 정보 수정 예시",
                      value =
                          """
                          {
                            "id": 101,
                            "name": "Spring 심화",
                            "type": "QUESTION_SET",
                            "scope": "CUSTOM",
                            "sortOrder": 1
                          }
                          """))),
  @ApiResponse(
      responseCode = "400",
      description = "유효하지 않은 입력값입니다.",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
  @ApiResponse(
      responseCode = "404",
      description = "수정할 폴더를 찾을 수 없거나 접근 권한이 없는 경우",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface UpdateFolderApiDocs {}
