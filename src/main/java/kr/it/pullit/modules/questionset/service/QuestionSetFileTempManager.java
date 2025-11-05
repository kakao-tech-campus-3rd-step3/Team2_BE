package kr.it.pullit.modules.questionset.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuestionSetFileTempManager {

  public void cleanUp(List<Path> paths) {
    if (paths == null || paths.isEmpty()) {
      return;
    }

    log.info("임시 파일 정리를 시작합니다. 대상 파일 {}개", paths.size());
    for (Path path : paths) {
      deleteIfExists(path);
    }
    log.info("임시 파일 정리를 완료했습니다.");
  }

  private void deleteIfExists(Path path) {
    try {
      if (Files.exists(path)) {
        Files.delete(path);
        log.debug("임시 파일을 삭제했습니다: {}", path);
      }
    } catch (IOException e) {
      log.warn("임시 파일 삭제에 실패했습니다: {}", path, e);
    }
  }
}
