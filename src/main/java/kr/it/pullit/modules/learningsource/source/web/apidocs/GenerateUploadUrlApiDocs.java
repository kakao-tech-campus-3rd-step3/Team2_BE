package kr.it.pullit.modules.learningsource.source.web.apidocs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import org.springframework.http.ProblemDetail;

// TODO: 상태코드 정상화
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
    summary = "S3 파일 업로드 URL 생성",
    description =
        """
            S3에 파일을 업로드하기 위한 Presigned URL을 생성합니다.
            클라이언트는 이 URL을 사용하여 서버를 거치지 않고 S3에 직접 파일을 업로드할 수 있습니다.

            [Request]
            - `SourceUploadRequest`: 업로드할 파일 정보 (Body, 필수)
              - `fileName`: 파일 원본 이름 (예: `document.pdf`)
              - `contentType`: 파일 MIME 타입 (예: `application/pdf`)
              - `fileSize`: 파일 크기 (바이트 단위)
            - 인증 토큰 필요 (Bearer)

            [Response]
            - 성공 시, 업로드에 필요한 정보(`uploadUrl`, `filePath` 등)를 담은 `SourceUploadResponse` 객체를 반환합니다.""")
@ApiResponses({
  @ApiResponse(
      responseCode = "200",
      description = "URL 생성 성공",
      content =
          @Content(
              schema = @Schema(implementation = SourceUploadResponse.class),
              examples =
                  @ExampleObject(
                      name = "S3 Presigned URL 생성 응답",
                      value =
                          """
                          {
                            "uploadUrl": "https://s3.ap-northeast-2.amazonaws.com/pullit/source/1/2025/11/10/...",
                            "filePath": "source/1/2025/11/10/generated-uuid-filename.pdf",
                            "originalName": "my-document.pdf",
                            "contentType": "application/pdf",
                            "fileSizeBytes": 204800,
                            "uploadId": "18c9c8be-e5d4-4c37-b0c4-ccf3c4fc4b65"
                          }
                          """))),
  @ApiResponse(
      responseCode = "400",
      description = "파일 크기 제한 초과",
      content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public @interface GenerateUploadUrlApiDocs {}
