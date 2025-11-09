package kr.it.pullit.modules.learningsource.source.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.domain.entity.SourceCreationParam;
import kr.it.pullit.modules.learningsource.source.exception.S3FileNotFoundForSourceException;
import kr.it.pullit.modules.learningsource.source.exception.SourceFileSizeExceededException;
import kr.it.pullit.modules.learningsource.source.exception.SourceNotFoundException;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceResponse;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadCompleteRequest;
import kr.it.pullit.modules.learningsource.source.web.dto.SourceUploadResponse;
import kr.it.pullit.modules.learningsource.sourcefolder.api.SourceFolderPublicApi;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.api.MemberPublicApi;
import kr.it.pullit.modules.member.exception.MemberNotFoundException;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import kr.it.pullit.platform.storage.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SourceService implements SourcePublicApi {

  private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20MiB

  private final S3PublicApi s3PublicApi;
  private final SourceRepository sourceRepository;
  private final MemberPublicApi memberPublicApi;
  private final SourceFolderPublicApi sourceFolderPublicApi;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public SourceUploadResponse generateUploadUrl(
      String fileName, String contentType, Long fileSize, Long memberId) {
    validateFileSize(fileSize);

    PresignedUrlResponse response =
        s3PublicApi.generateUploadUrl(fileName, contentType, fileSize, memberId);

    return new SourceUploadResponse(
        response.uploadUrl(), response.filePath(), fileName, fileSize, contentType);
  }

  private void validateFileSize(Long fileSize) {
    if (fileSize > MAX_FILE_SIZE_BYTES) {
      throw new SourceFileSizeExceededException(fileSize, MAX_FILE_SIZE_BYTES);
    }
  }

  /** 이 서비스를 이용하기 전에 클라이언트는 S3 서비스에 파일을 업로드한 상태여야 한다. */
  // TODO: 리팩토링 대상.
  @Override
  public void processUploadComplete(SourceUploadCompleteRequest request, Long memberId) {
    s3PublicApi.getFileMetadata(request.getFilePath());
    createOrUpdate(request, memberId);
  }

  private void createOrUpdate(SourceUploadCompleteRequest request, Long memberId) {
    Source source = findSourceBy(request, memberId);
    source.updateFileInfo(
        request.getOriginalName(), request.getContentType(), request.getFileSizeBytes());
    source.markAsReady(); // 상태를 READY로 변경
    sourceRepository.save(source);
  }

  private Source findSourceBy(SourceUploadCompleteRequest request, Long memberId) {
    return sourceRepository
        .findByMemberIdAndFilePath(memberId, request.getFilePath())
        .orElseGet(() -> create(request, memberId));
  }

  private Source create(SourceUploadCompleteRequest request, Long memberId) {
    memberPublicApi.findById(memberId).orElseThrow(() -> MemberNotFoundException.byId(memberId));
    SourceFolder sourceFolder = findSourceFolderBy(memberId);
    SourceCreationParam sourceCreationParam =
        new SourceCreationParam(
            memberId,
            request.getOriginalName(),
            request.getFilePath(),
            request.getContentType(),
            request.getFileSizeBytes());

    return Source.create(sourceCreationParam, memberId, sourceFolder);
  }

  private SourceFolder findSourceFolderBy(Long memberId) {
    return sourceFolderPublicApi.findOrCreateDefaultFolder(memberId);
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

    try {
      return s3PublicApi.downloadFileAsStream(source.getFilePath());
    } catch (NoSuchKeyException e) {
      markSourceAsNotExist(source.getId());
      throw S3FileNotFoundForSourceException.bySourceIdAndFilePath(sourceId, source.getFilePath());
    }
  }

  @Override
  public Path downloadFileToTemp(long sourceId, long memberId) throws IOException {
    Source source =
        sourceRepository
            .findByIdAndMemberId(sourceId, memberId)
            .orElseThrow(() -> SourceNotFoundException.byId(sourceId));

    try {
      return s3PublicApi.downloadFileToTemp(source.getFilePath());
    } catch (NoSuchKeyException e) {
      markSourceAsNotExist(source.getId());
      throw S3FileNotFoundForSourceException.bySourceIdAndFilePath(sourceId, source.getFilePath());
    }
  }

  @Override
  public Optional<Source> findById(Long id) {
    return sourceRepository.findById(id);
  }

  @Override
  public Optional<Source> findByIdAndMemberId(Long id, Long memberId) {
    return sourceRepository.findByIdAndMemberId(id, memberId);
  }

  @Override
  public List<Source> findByIdIn(List<Long> ids) {
    return sourceRepository.findByIdIn(ids);
  }

  /**
   * 소스 삭제 시 문제집과의 연관 관계를 자동으로 제거해준다. @SQLDelete 어노테이션에 의해 soft delete 처리된다. S3 파일 삭제는 별도의 배치 작업으로
   * 처리한다. Source 엔티티의 @PreRemove 콜백이 QuestionSet과의 연관 관계를 자동으로 제거해준다.
   */
  @Override
  public void deleteSource(Long sourceId, Long memberId) {
    Source source = getOrElseThrow(sourceId, memberId);

    sourceRepository.delete(source);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSourceAsError(Long sourceId) {
    sourceRepository
        .findById(sourceId)
        .ifPresent(
            source -> {
              source.markAsError();
              log.warn("Source ID {}의 상태를 ERROR로 변경했습니다.", sourceId);
            });
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markSourceAsNotExist(Long sourceId) {
    sourceRepository
        .findById(sourceId)
        .ifPresent(
            source -> {
              source.markAsNotExist();
              log.warn("Source ID {}의 상태를 NOT_EXIST로 변경했습니다.", sourceId);
            });
  }

  @Transactional
  public void synchronizeS3Files() {
    log.info("DB에 READY 상태이지만 S3에 존재하지 않는 Source 데이터 정리를 시작합니다.");
    List<Source> readySources = sourceRepository.findByStatus(SourceStatus.READY);

    int processedCount = 0;
    int inconsistentCount = 0;

    for (Source source : readySources) {
      processedCount++;
      if (!s3PublicApi.fileExists(source.getFilePath())) {
        inconsistentCount++;
        log.warn(
            "S3에 파일이 존재하지 않아 Source의 상태를 NOT_EXIST로 변경합니다. Source ID: {}, FilePath: {}",
            source.getId(),
            source.getFilePath());
        source.markAsNotExist();
      }
    }

    log.info(
        "Source 데이터 정리 완료. 총 {}개의 READY 상태 소스 중 {}개의 불일치 데이터를 처리했습니다.",
        processedCount,
        inconsistentCount);
  }

  @Transactional
  public void migrateUploadedSourcesToReady() {
    log.info("기존 UPLOADED 상태의 소스 데이터 마이그레이션을 시작합니다.");
    List<Source> uploadedSources = sourceRepository.findByStatus(SourceStatus.UPLOADED);

    int successCount = processMigrationForSources(uploadedSources);

    log.info(
        "소스 데이터 마이그레이션을 완료했습니다. 총 {}개의 소스 중 {}개의 상태를 READY로 변경했습니다.",
        uploadedSources.size(),
        successCount);
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
      log.error(
          "마이그레이션 중 Source ID {} 처리 오류 발생. 파일 경로: {}", source.getId(), source.getFilePath(), e);
      return false;
    }
  }

  private boolean migrateSourceIfFileExists(Source source) {
    if (s3PublicApi.fileExists(source.getFilePath())) {
      source.markAsReady();
      sourceRepository.save(source);
      log.info("Source ID {}의 상태를 READY로 변경했습니다. 파일 경로: {}", source.getId(), source.getFilePath());
      return true;
    }

    log.warn(
        "S3에 파일이 존재하지 않아 Source ID {}의 상태를 변경하지 않았습니다. 파일 경로: {}",
        source.getId(),
        source.getFilePath());
    return false;
  }

  private Source getOrElseThrow(Long sourceId, Long memberId) {
    return sourceRepository
        .findByIdAndMemberId(sourceId, memberId)
        .orElseThrow(() -> SourceNotFoundException.byId(sourceId));
  }
}
