package kr.it.pullit.modules.learningsource.source.service;

import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.source.web.dto.UploadCompleteRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.UploadResponse;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.platform.storage.s3.client.FileStorageClient;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceService implements SourcePublicApi {

  private final S3PublicApi s3PublicApi;
  private final FileStorageClient fileStorageClient;
  private final SourceRepository sourceRepository;

  @Override
  public UploadResponse generateUploadUrl(String fileName, String contentType, Long fileSize,
      Long memberId) {
    PresignedUrlResponse response =
        s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    return new UploadResponse(response.uploadUrl(), response.filePath(), fileName, contentType,
        fileSize);
  }

  /**
   * 이 서비스를 이용하기 전에 클라이언트는 S3 서비스에 파일을 업로드한 상태여야 한다.
   */
  @Override
  public void processUploadComplete(UploadCompleteRequest request, Long memberId) {
    if (!fileStorageClient.fileExists(request.getFilePath())) {
      throw new IllegalArgumentException("S3에 해당 파일이 존재하지 않습니다.");
    }

    var param = new SourceCreationParam(memberId, request.getOriginalName(), request.getFilePath(),
        request.getContentType(), request.getFileSizeBytes());

    Source source = Source.create(param);

    sourceRepository.save(source);
  }
}
