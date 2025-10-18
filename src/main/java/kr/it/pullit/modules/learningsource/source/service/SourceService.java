package kr.it.pullit.modules.learningsource.source.service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
  // TODO: 리팩토링 대상.
  @Override
  public void processUploadComplete(SourceUploadCompleteRequest request, Long memberId) {
    if (!s3PublicApi.fileExists(request.getFilePath())) {
      throw new IllegalArgumentException("S3에 해당 파일이 존재하지 않습니다.");
    }

    Source source =
        sourceRepository
            .findByMemberIdAndFilePath(memberId, request.getFilePath())
            .orElseGet(
                () -> {
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

                  SourceFolder sourceFolder =
                      sourceFolderPublicApi.findOrCreateDefaultFolder(memberId);

                  return Source.create(sourceCreationParam, member, sourceFolder);
                });

    source.updateFileInfo(
        request.getOriginalName(), request.getContentType(), request.getFileSizeBytes());
    source.markAsReady(); // 상태를 READY로 변경

    sourceRepository.save(source);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SourceResponse> getMySources(Long memberId) {
    return sourceRepository.findSourcesByMemberIdWithDetails(memberId).stream()
        .map(SourceResponse::from)
        .toList();
  }

  @Override
  public InputStream getContentStream(Long sourceId, Long memberId) {
    Source source =
        sourceRepository
            .findByIdAndMemberId(sourceId, memberId)
            .orElseThrow(() -> SourceNotFoundException.byId(sourceId));

    return s3PublicApi.downloadFileAsStream(source.getFilePath());
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

  @Override
  public void deleteSource(Long sourceId, Long memberId) {
    Source source = getOrElseThrow(sourceId, memberId);
    String filePath = source.getFilePath();
    sourceRepository.delete(source);
    deleteInS3(filePath);
  }

  @Transactional
  public void migrateUploadedSourcesToReady() {
    log.info("기존 UPLOADED 상태의 소스 데이터 마이그레이션을 시작합니다.");
    List<Source> uploadedSources = findUploadedSources();

    int successCount = processMigrationForSources(uploadedSources);

    logMigrationSummary(uploadedSources.size(), successCount);
  }

  private List<Source> findUploadedSources() {
    return sourceRepository.findByStatus(SourceStatus.UPLOADED);
  }

  private int processMigrationForSources(List<Source> sources) {
    int successCount = 0;
    for (Source source : sources) {
      if (tryMigrateSingleSource(source)) {
        successCount++;
      }
    }
    return successCount;
  }

  private boolean tryMigrateSingleSource(Source source) {
    try {
      return migrateSourceIfFileExists(source);
    } catch (Exception e) {
      logMigrationError(source, e);
      return false;
    }
  }

  private boolean migrateSourceIfFileExists(Source source) {
    if (s3PublicApi.fileExists(source.getFilePath())) {
      updateSourceStatusToReady(source);
      logMigrationSuccess(source);
      return true;
    }

    logS3FileNotFound(source);
    return false;
  }

  private void updateSourceStatusToReady(Source source) {
    source.markAsReady();
    sourceRepository.save(source);
  }

  private void logMigrationSuccess(Source source) {
    log.info("Source ID {}의 상태를 READY로 변경했습니다. 파일 경로: {}", source.getId(), source.getFilePath());
  }

  private void logS3FileNotFound(Source source) {
    log.warn(
        "S3에 파일이 존재하지 않아 Source ID {}의 상태를 변경하지 않았습니다. 파일 경로: {}",
        source.getId(),
        source.getFilePath());
  }

  private void logMigrationError(Source source, Exception e) {
    log.error("마이그레이션 중 Source ID {} 처리 오류 발생. 파일 경로: {}", source.getId(), source.getFilePath(), e);
  }

  private void logMigrationSummary(int total, int success) {
    log.info("소스 데이터 마이그레이션을 완료했습니다. 총 {}개의 소스 중 {}개의 상태를 READY로 변경했습니다.", total, success);
  }

  private Source getOrElseThrow(Long sourceId, Long memberId) {
    return sourceRepository
        .findByIdAndMemberId(sourceId, memberId)
        .orElseThrow(() -> SourceNotFoundException.byId(sourceId));
  }

  private void deleteInS3(String filePath) {
    try {
      s3PublicApi.deleteFile(filePath);
    } catch (Exception e) {
      log.warn("데이터베이스 삭제 후 S3 파일 삭제 실패: {}", filePath, e);
    }
  }
}
