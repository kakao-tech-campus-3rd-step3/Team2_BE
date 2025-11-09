package kr.it.pullit.modules.questionset.exception;

import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.modules.learningsource.source.exception.SourceErrorCode;
import kr.it.pullit.shared.error.BusinessException;

public class SourceNotExistOnS3Exception extends BusinessException {

  public SourceNotExistOnS3Exception(List<Source> notExistSources) {
    super(SourceErrorCode.SOURCE_NOT_EXIST_ON_S3, getSourceIds(notExistSources));
  }

  private static String getSourceIds(List<Source> sources) {
    return sources.stream()
        .map(Source::getId)
        .map(String::valueOf)
        .collect(Collectors.joining(", "));
  }
}
