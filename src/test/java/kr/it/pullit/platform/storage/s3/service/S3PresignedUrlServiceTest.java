package kr.it.pullit.platform.storage.s3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import kr.it.pullit.platform.storage.core.FilePathPolicy;
import kr.it.pullit.platform.storage.core.FileValidation;
import kr.it.pullit.platform.storage.core.S3StorageProps;
import kr.it.pullit.platform.storage.s3.client.FileStorageClient;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;
import kr.it.pullit.support.annotation.MockitoUnitTest;

@MockitoUnitTest
@DisplayName("S3PresignedUrlService 단위 테스트")
class S3PresignedUrlServiceTest {

    @InjectMocks
    private S3PresignedUrlService s3PresignedUrlService;

    @Mock
    private FileStorageClient fileStorageClient;

    @Mock
    private FileValidation fileValidation;

    @Mock
    private FilePathPolicy filePathPolicy;

    @Mock
    private S3StorageProps s3StorageProps;

    @Mock
    private InputStream inputStream;

    @DisplayName("파일 업로드를 위한 Presigned URL을 생성한다")
    @Test
    void generateUploadUrl() throws MalformedURLException {
        // given
        String fileName = "test.pdf";
        String contentType = "application/pdf";
        Long fileSize = 1024L;
        Long memberId = 1L;
        String generatedFilePath = "user-files/1/test.pdf";
        URL presignedUrl = new URL("https://s3.amazonaws.com/bucket/user-files/1/test.pdf?presigned-url");

        given(filePathPolicy.generateFilePath(fileName, memberId)).willReturn(generatedFilePath);

        Duration duration = Duration.ofHours(1);
        given(s3StorageProps.getPresignedUrlExpiration()).willReturn(duration);
        given(fileStorageClient.generatePresignedUploadUrl(generatedFilePath, contentType, duration)).willReturn(presignedUrl);

        // when
        PresignedUrlResponse response = s3PresignedUrlService.generateUploadUrl(fileName, contentType, fileSize, memberId);

        // then
        verify(fileValidation).validatePdfFile(contentType, fileSize);
        verify(filePathPolicy).generateFilePath(fileName, memberId);
        verify(fileStorageClient).generatePresignedUploadUrl(generatedFilePath, contentType, duration);

        assertThat(response.uploadUrl()).isEqualTo(presignedUrl.toString());
        assertThat(response.filePath()).isEqualTo(generatedFilePath);
    }

    @DisplayName("파일 경로를 통해 파일을 스트림으로 다운로드한다")
    @Test
    void downloadFileAsStream() {
        // given
        String filePath = "user-files/1/test.pdf";
        given(fileStorageClient.downloadFileAsStream(filePath)).willReturn(inputStream);

        // when
        InputStream resultStream = s3PresignedUrlService.downloadFileAsStream(filePath);

        // then
        verify(fileStorageClient).downloadFileAsStream(filePath);
        assertThat(resultStream).isEqualTo(inputStream);
    }

    @DisplayName("파일 경로를 통해 파일 존재 여부를 확인한다")
    @Test
    void fileExists() {
        // given
        String filePath = "user-files/1/test.pdf";
        given(fileStorageClient.fileExists(filePath)).willReturn(true);

        // when
        boolean exists = s3PresignedUrlService.fileExists(filePath);

        // then
        verify(fileStorageClient).fileExists(filePath);
        assertThat(exists).isTrue();
    }

    @DisplayName("파일 경로를 통해 파일을 삭제한다")
    @Test
    void deleteFile() {
        // given
        String filePath = "user-files/1/test.pdf";

        // when
        s3PresignedUrlService.deleteFile(filePath);

        // then
        verify(fileStorageClient).deleteFile(filePath);
    }
}
