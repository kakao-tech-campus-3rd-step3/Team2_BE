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
import kr.it.pullit.modules.commonfolder.web.dto.FolderDeleteWarningResponse;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "공통 폴더 삭제 경고 조회",
    description = "폴더 삭제 전에 포함된 문제집 개수를 확인합니다.",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponse(
    responseCode = "200",
    description = "삭제 경고 조회 성공",
    content =
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = FolderDeleteWarningResponse.class),
            examples =
                @ExampleObject(
                    name = "폴더 삭제 경고",
                    summary = "폴더에 포함된 문제집 수",
                    value =
                        """
                        {
                          \"questionSetCount\": 3
                        }
                        """)))
public @interface GetFolderDeleteWarningApiDocs {}
