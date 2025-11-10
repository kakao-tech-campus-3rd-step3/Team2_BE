package kr.it.pullit.modules.commonfolder.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.http.ProblemDetail;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "폴더 생성")
@ApiResponses({
  @ApiResponse(responseCode = "201", description = "생성 성공"),
  @ApiResponse(
      responseCode = "400",
      description = "유효하지 않은 입력값입니다.",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface CreateFolderApiDocs {}
