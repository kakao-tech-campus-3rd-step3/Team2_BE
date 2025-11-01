package kr.it.pullit.modules.questionset.exception;

import java.util.List;
import java.util.stream.Collectors;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import kr.it.pullit.shared.error.BusinessException;

public class SourceNotReadyException extends BusinessException {

  public SourceNotReadyException(List<Source> notReadySources) {
    super(QuestionSetErrorCode.SOURCE_NOT_READY, getNotReadySourceIds(notReadySources));
  }

  private static String getNotReadySourceIds(List<Source> notReadySources) {
    return notReadySources.stream()
        .map(Source::getId)
        .map(String::valueOf)
        .collect(Collectors.joining(", "));
  }
}
