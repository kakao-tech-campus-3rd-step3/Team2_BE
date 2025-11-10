package kr.it.pullit.modules.commonfolder.web.apidocs;

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
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "공통 폴더 목록 조회",
    description =
        """
            타입별 공통 폴더 목록을 조회합니다.
            '공통 폴더'는 시스템에 미리 정의된 기본 폴더를 의미합니다.

            [Request]
            - `type`: 조회할 폴더 타입 (Query Param, 필수, 예: `QUESTION_SET` 또는 `LEARNING_SOURCE`)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 해당 타입의 폴더 목록을 배열 형태로 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponses(
    value = {
      @ApiResponse(
          responseCode = "200",
          description = "폴더 목록 조회 성공",
          content =
              @Content(
                  mediaType = "application/json",
                  array =
                      @ArraySchema(schema = @Schema(implementation = CommonFolderResponse.class)),
                  examples =
                      @ExampleObject(
                          name = "공통 폴더 목록 예시",
                          value =
                              """
                              [
                                {
                                  "id": 1,
                                  "name": "기본 폴더",
                                  "type": "QUESTION_SET",
                                  "scope": "ALL",
                                  "sortOrder": 0
                                },
                                {
                                  "id": 2,
                                  "name": "자주 쓰는 문제집",
                                  "type": "QUESTION_SET",
                                  "scope": "ALL",
                                  "sortOrder": 1
                                }
                              ]
                              """)))
    })
public @interface GetCommonFoldersApiDocs {}
