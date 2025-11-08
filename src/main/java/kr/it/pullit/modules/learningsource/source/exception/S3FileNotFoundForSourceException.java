package kr.it.pullit.modules.learningsource.source.exception;

import kr.it.pullit.shared.error.BusinessException;

public class S3FileNotFoundForSourceException extends BusinessException {

  private S3FileNotFoundForSourceException(Object... args) {
    super(SourceErrorCode.S3_FILE_NOT_FOUND, args);
  }

  public static S3FileNotFoundForSourceException bySourceIdAndFilePath(
      Long sourceId, String filePath) {
    return new S3FileNotFoundForSourceException(sourceId, filePath);
  }
}
