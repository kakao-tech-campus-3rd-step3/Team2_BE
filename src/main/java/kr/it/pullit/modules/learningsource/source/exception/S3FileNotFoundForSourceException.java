package kr.it.pullit.modules.learningsource.source.exception;

import kr.it.pullit.shared.error.BusinessException;

public class S3FileNotFoundForSourceException extends BusinessException {

  private S3FileNotFoundForSourceException(Object... args) {
    super(SourceErrorCode.S3_FILE_NOT_FOUND, args);
  }

  public static S3FileNotFoundForSourceException bySourceIdAndFilePath(
      Long sourceId, String filePath) {
    return new S3FileNotFoundForSourceException(
        "S3에 파일이 존재하지 않습니다. (sourceId: %d, filePath: '%s')", sourceId, filePath);
  }
}
