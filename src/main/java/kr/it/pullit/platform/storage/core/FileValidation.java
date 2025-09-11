package kr.it.pullit.platform.storage.core;

import org.springframework.stereotype.Component;

@Component
public class FileValidation {

  private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
  private static final String[] ALLOWED_CONTENT_TYPES = {"application/pdf"};
  private static final String[] ALLOWED_EXTENSIONS = {".pdf"};

  public void validatePdfFile(String fileName, String contentType, long fileSize) {
    validateFileSize(fileSize);
    validateContentType(contentType);
    validateFileExtension(fileName);
  }

  private void validateFileSize(long fileSize) {
    if (fileSize > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 50MB까지 업로드 가능합니다.");
    }
    if (fileSize <= 0) {
      throw new IllegalArgumentException("유효하지 않은 파일 크기입니다.");
    }
  }

  private void validateContentType(String contentType) {
    if (contentType == null) {
      throw new IllegalArgumentException("파일 타입이 지정되지 않았습니다.");
    }

    for (String allowedType : ALLOWED_CONTENT_TYPES) {
      if (allowedType.equals(contentType)) {
        return;
      }
    }
    throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
  }

  private void validateFileExtension(String fileName) {
    if (fileName == null) {
      throw new IllegalArgumentException("파일명이 지정되지 않았습니다.");
    }

    for (String allowedExt : ALLOWED_EXTENSIONS) {
      if (fileName.toLowerCase().endsWith(allowedExt)) {
        return;
      }
    }
    throw new IllegalArgumentException("PDF 확장자 파일만 업로드 가능합니다.");
  }
}
