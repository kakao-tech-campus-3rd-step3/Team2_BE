package kr.it.pullit.modules.commonfolder.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "내 폴더 목록 조회",
    description =
        """
            인증된 사용자의 특정 타입에 해당하는 폴더 목록을 정렬 순서에 따라 조회합니다.
            '공통 폴더'와 달리 사용자가 직접 생성하고 관리하는 폴더 목록입니다.

            [Request]
            - `type`: `QUESTION_SET` 또는 `LEARNING_SOURCE` (Query Param, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 해당 타입의 폴더 목록을 배열 형태로 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponse(
    responseCode = "200",
    description = "폴더 목록 조회 성공",
    content =
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CommonFolderResponse.class, type = "array"),
            examples =
                @ExampleObject(
                    name = "내 폴더 목록 예시",
                    value =
                        """
                        [
                          {
                            "id": 101,
                            "name": "JPA 심화",
                            "type": "QUESTION_SET",
                            "scope": "CUSTOM",
                            "sortOrder": 1
                          },
                          {
                            "id": 102,
                            "name": "Spring Core",
                            "type": "QUESTION_SET",
                            "scope": "CUSTOM",
                            "sortOrder": 2
                          }
                        ]
                        """)))
public @interface GetFoldersApiDocs {}
