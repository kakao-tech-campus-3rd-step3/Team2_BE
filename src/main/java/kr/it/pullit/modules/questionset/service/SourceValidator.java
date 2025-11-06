package kr.it.pullit.modules.questionset.service;

import java.util.List;
import kr.it.pullit.modules.learningsource.source.api.SourcePublicApi;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.questionset.exception.SourceNotReadyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceValidator {

  private final SourcePublicApi sourcePublicApi;

  public void validateSourcesAreReady(List<Long> sourceIds, Long questionSetId) {
    if (sourceIds.isEmpty()) {
      return;
    }

    List<Source> sources = fetchAndVerifySources(sourceIds, questionSetId);
    List<Source> notReadySources = filterNotReadySources(sources);

    handleNotReadySources(notReadySources, questionSetId);
  }

  private List<Source> fetchAndVerifySources(List<Long> sourceIds, Long questionSetId) {
    List<Source> sources = sourcePublicApi.findByIdIn(sourceIds);
    if (sources.size() != sourceIds.size()) {
      log.warn("요청된 소스 ID 중 일부를 DB에서 찾을 수 없습니다. QuestionSet ID: {}", questionSetId);
    }
    return sources;
  }

  private List<Source> filterNotReadySources(List<Source> sources) {
    return sources.stream().filter(source -> source.getStatus() != SourceStatus.READY).toList();
  }

  private void handleNotReadySources(List<Source> notReadySources, Long questionSetId) {
    if (!notReadySources.isEmpty()) {
      throw createException(notReadySources);
    }
  }

  private SourceNotReadyException createException(List<Source> notReadySources) {
    return new SourceNotReadyException(notReadySources);
  }
}
