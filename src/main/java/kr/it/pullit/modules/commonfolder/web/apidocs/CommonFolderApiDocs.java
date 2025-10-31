package kr.it.pullit.modules.commonfolder.web.apidocs;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.ProblemDetail;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
    responseCode = "401",
    description = "인증 실패",
    content =
        @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProblemDetail.class),
            examples =
                @ExampleObject(
                    name = "인증 실패",
                    value =
                        """
                            {
                              "type": "about:blank",
                              "title": "Unauthorized",
                              "status": 401,
                              "detail": "인증에 실패했습니다.",
                              "instance": "/api/common-folders",
                              "code": "TOKEN_999"
                            }
                            """)))
public @interface CommonFolderApiDocs {}
