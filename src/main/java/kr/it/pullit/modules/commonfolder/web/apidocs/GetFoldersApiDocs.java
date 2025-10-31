package kr.it.pullit.modules.commonfolder.web.apidocs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "내 폴더 목록 조회",
    description =
        """
            인증된 사용자의 특정 타입에 해당하는 폴더 목록을 정렬 순서에 따라 조회합니다.

            [Request]
            - `type`: `QUESTION_SET` 또는 `LEARNING_SOURCE` (필수)
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
                    name = "폴더 목록 응답",
                    value =
                        """
                        [
                          {
                            "id": 1,
                            "name": "전체",
                            "type": "QUESTION_SET",
                            "sortOrder": 0
                          },
                          {
                            "id": 2,
                            "name": "JPA",
                            "type": "QUESTION_SET",
                            "sortOrder": 1
                          }
                        ]
                        """)))
public @interface GetFoldersApiDocs {}
