package kr.it.pullit.modules.learningsource.source.scheduler;

import java.util.List;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.repository.SourceRepository;
import kr.it.pullit.platform.storage.api.S3PublicApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SourceCleanupService {

  private final SourceRepository sourceRepository;
  private final S3PublicApi s3PublicApi;

  @Transactional
  public void cleanupDeletedSources() {
    log.info("삭제 상태(DELETED)인 Source 정리를 시작합니다.");

    List<Source> deletedSources = sourceRepository.findAllWithDeleted();
    if (deletedSources.isEmpty()) {
      log.info("정리할 Source가 없습니다.");
      return;
    }

    int successCount = 0;
    for (Source source : deletedSources) {
      try {
        s3PublicApi.deleteFile(source.getFilePath());
        sourceRepository.hardDelete(source);
        successCount++;
        log.info(
            "Source(id:{}, path:{})가 S3와 DB에서 완전히 삭제되었습니다.", source.getId(), source.getFilePath());
      } catch (Exception e) {
        log.error(
            "Source(id:{}, path:{}) 최종 삭제 처리 중 오류가 발생했습니다.",
            source.getId(),
            source.getFilePath(),
            e);
      }
    }
    log.info("Source 정리 작업을 완료했습니다. 총 {}개 중 {}개 성공.", deletedSources.size(), successCount);
  }
}
