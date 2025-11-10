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
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "공통 폴더 단건 조회",
    description =
        """
            폴더 식별자로 공통 폴더의 상세 정보를 조회합니다.

            [Request]
            - `id`: 조회할 폴더 ID (Path Variable, 필수)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 조회된 폴더의 상세 정보를 반환합니다.""",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponse(
    responseCode = "200",
    description = "폴더 조회 성공",
    content =
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CommonFolderResponse.class),
            examples =
                @ExampleObject(
                    name = "공통 폴더 조회 예시",
                    value =
                        """
                        {
                          "id": 1,
                          "name": "Java 기초",
                          "type": "QUESTION_SET",
                          "scope": "ALL",
                          "sortOrder": 1
                        }
                        """)))
public @interface GetCommonFolderApiDocs {}
