package kr.it.pullit.modules.questionset.exception;

import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.exception.SourceErrorCode;
import kr.it.pullit.shared.error.BusinessException;

public class SourceProcessingFailedException extends BusinessException {

  public SourceProcessingFailedException(List<Source> failedSources) {
    super(SourceErrorCode.SOURCE_PROCESSING_FAILED, getSourceIds(failedSources));
  }

  private static String getSourceIds(List<Source> sources) {
    return sources.stream()
        .map(Source::getId)
        .map(String::valueOf)
        .collect(Collectors.joining(", "));
  }
}
