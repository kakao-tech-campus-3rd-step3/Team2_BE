package kr.it.pullit.modules.commonfolder.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.commonfolder.web.dto.CommonFolderResponse;
import kr.it.pullit.modules.commonfolder.web.dto.UpdateFolderRequest;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "공통 폴더 수정",
    description = "기존 공통 폴더의 이름과 타입을 수정합니다.",
    security = @SecurityRequirement(name = "bearerAuth"),
    requestBody =
        @RequestBody(
            required = true,
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateFolderRequest.class))))
@ApiResponse(
    responseCode = "200",
    description = "폴더 수정 성공",
    content =
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CommonFolderResponse.class),
            examples =
                @ExampleObject(
                    name = "폴더 수정 응답 예시",
                    value =
                        """
                        {
                          "id": 5,
                          "name": "심화 학습",
                          "type": "QUESTION_SET",
                          "scope": "ALL",
                          "sortOrder": 0
                        }
                        """)))
public @interface UpdateCommonFolderApiDocs {}
