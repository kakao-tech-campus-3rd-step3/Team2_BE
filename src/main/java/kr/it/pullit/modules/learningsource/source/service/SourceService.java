package kr.it.pullit.modules.learningsource.source.service;

import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceResponse;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
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
  public SourceUploadResponse generateUploadUrl(String fileName, String contentType, Long fileSize,
      Long memberId) {
    PresignedUrlResponse response =
        s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    return new SourceUploadResponse(response.uploadUrl(), response.filePath(), fileName,
        contentType, fileSize);
  }

  /**
   * 이 서비스를 이용하기 전에 클라이언트는 S3 서비스에 파일을 업로드한 상태여야 한다.
   */
  @Override
  public void processUploadComplete(SourceUploadCompleteRequest request, Long memberId) {
    if (!fileStorageClient.fileExists(request.getFilePath())) {
      throw new IllegalArgumentException("S3에 해당 파일이 존재하지 않습니다.");
    }

    if (request.getUploadId() == null || request.getUploadId().isBlank()) {
      throw new IllegalArgumentException("유효하지 않은 업로드 세션입니다.");
    }

    SourceCreationParam sourceCreationParam =
        new SourceCreationParam(memberId, request.getOriginalName(), request.getFilePath(),
            request.getContentType(), request.getFileSizeBytes());

    Source source = Source.create(sourceCreationParam);

    sourceRepository.save(source);
  }

  @Override
  public List<SourceResponse> getMySources(Long memberId) {
    return sourceRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
        .map(SourceResponse::from).collect(Collectors.toList());
  }
}
