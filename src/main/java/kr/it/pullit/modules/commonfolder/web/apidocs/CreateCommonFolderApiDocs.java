package kr.it.pullit.modules.commonfolder.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
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
import kr.it.pullit.modules.commonfolder.web.dto.QuestionSetFolderRequest;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "공통 폴더 생성",
    description = "새로운 공통 폴더를 생성합니다.\n\n" + "[Request]\n" + "- 폴더 이름과 타입을 입력합니다.",
    security = @SecurityRequirement(name = "bearerAuth"),
    requestBody =
        @RequestBody(
            required = true,
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = QuestionSetFolderRequest.class),
                    examples =
                        @ExampleObject(
                            name = "폴더 생성 요청",
                            summary = "QUESTION_SET 폴더 생성",
                            value =
                                """
                                {
                                  \"name\": \"개념 정리\",
                                  \"type\": \"QUESTION_SET\"
                                }
                                """))))
@ApiResponse(
    responseCode = "201",
    description = "폴더 생성 성공",
    headers =
        @Header(
            name = "Location",
            description = "생성된 폴더 리소스 URI",
            schema = @Schema(type = "string"),
            example = "/api/common-folders/10"))
public @interface CreateCommonFolderApiDocs {}
