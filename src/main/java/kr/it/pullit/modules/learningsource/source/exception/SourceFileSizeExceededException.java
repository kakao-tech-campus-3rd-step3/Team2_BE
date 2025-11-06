package kr.it.pullit.modules.learningsource.source.exception;

import static kr.it.pullit.modules.learningsource.source.exception.SourceErrorCode.FILE_SIZE_EXCEEDED;

import kr.it.pullit.shared.error.BusinessException;

public class SourceFileSizeExceededException extends BusinessException {

  public SourceFileSizeExceededException(long actualSize, long maxSize) {
    super(
        FILE_SIZE_EXCEEDED,
        String.format("파일 크기 제한(%d bytes)을 초과했습니다. (현재 파일 크기: %d bytes)", maxSize, actualSize));
  }
}
