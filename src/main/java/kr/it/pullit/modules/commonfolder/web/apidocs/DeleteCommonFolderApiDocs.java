package kr.it.pullit.modules.commonfolder.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.shared.apidocs.ApiDocsGroup;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiDocsGroup
@Operation(
    summary = "공통 폴더 삭제",
    description = "공통 폴더와 해당 폴더에 속한 모든 데이터를 삭제합니다.",
    security = @SecurityRequirement(name = "bearerAuth"))
@ApiResponse(responseCode = "204", description = "폴더 삭제 성공")
public @interface DeleteCommonFolderApiDocs {}
