package kr.it.pullit.modules.learningsource.source.service;

import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.exception.SourceNotFoundException;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceResponse;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import kr.it.pullit.modules.learningsource.sourcefolder.api.SourceFolderPublicApi;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.platform.storage.s3.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class SourceService implements SourcePublicApi {

  private final S3PublicApi s3PublicApi;
  private final SourceRepository sourceRepository;
  private final MemberPublicApi memberPublicApi;
  private final SourceFolderPublicApi sourceFolderPublicApi;

  @Override
  public SourceUploadResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId) {
    PresignedUrlResponse response =
        s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    return new SourceUploadResponse(
        response.uploadUrl(), response.filePath(), fileName, fileSize, contentType);
  }

  /** 이 서비스를 이용하기 전에 클라이언트는 S3 서비스에 파일을 업로드한 상태여야 한다. */
  @Override
  public void processUploadComplete(SourceUploadCompleteRequest request, Long memberId) {
    if (!s3PublicApi.fileExists(request.getFilePath())) {
      throw new IllegalArgumentException("S3에 해당 파일이 존재하지 않습니다.");
    }

    Optional<Source> existingSource =
        sourceRepository.findByMemberIdAndFilePath(memberId, request.getFilePath());

    if (existingSource.isPresent()) {
      Source source = existingSource.get();
      source.updateFileInfo(
          request.getOriginalName(), request.getContentType(), request.getFileSizeBytes());
      sourceRepository.save(source);
    } else {
      SourceCreationParam sourceCreationParam =
          new SourceCreationParam(
              memberId,
              request.getOriginalName(),
              request.getFilePath(),
              request.getContentType(),
              request.getFileSizeBytes());

      Member member =
          memberPublicApi
              .findById(memberId)
              .orElseThrow(() -> MemberNotFoundException.byId(memberId));

      SourceFolder sourceFolder = sourceFolderPublicApi.findOrCreateDefaultFolder(memberId);

      Source source = Source.create(sourceCreationParam, member, sourceFolder);

      sourceRepository.save(source);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<SourceResponse> getMySources(Long memberId) {
    return sourceRepository.findSourcesByMemberIdWithDetails(memberId).stream()
        .map(SourceResponse::from)
        .toList();
  }

  @Override
  public byte[] getContentBytes(Long sourceId, Long memberId) {
    Source source =
        sourceRepository
            .findByIdAndMemberId(sourceId, memberId)
            .orElseThrow(() -> SourceNotFoundException.byId(sourceId));

    return s3PublicApi.downloadFileAsBytes(source.getFilePath());
  }

  @Override
  public Optional<Source> findById(Long id) {
    return sourceRepository.findById(id);
  }

  @Override
  public Optional<Source> findByIdAndMemberId(Long id) {
    return sourceRepository.findByIdAndMemberId(id, id);
  }

  @Override
  public List<Source> findByIdIn(List<Long> ids) {
    return sourceRepository.findByIdIn(ids);
  }

  public void deleteSource(Long sourceId, Long memberId) {
    Source source =
        sourceRepository
            .findById(sourceId)
            .orElseThrow(() -> SourceNotFoundException.byId(sourceId));

    if (!source.getMember().getId().equals(memberId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 소스가 아니므로 삭제할 수 없습니다.");
    }

    sourceRepository.delete(source);

    s3PublicApi.deleteFile(source.getFilePath());
  }
}
