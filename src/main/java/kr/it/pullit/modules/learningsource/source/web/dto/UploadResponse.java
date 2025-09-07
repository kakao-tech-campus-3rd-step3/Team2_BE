package kr.it.pullit.modules.learningsource.source.web.dto;

import lombok.Getter;

@Getter
public class UploadResponse {

  private final String uploadUrl;
  private final String filePath;
  private final String originalName;
  private final String contentType;
  private final Long fileSizeBytes;

  public UploadResponse(String uploadUrl, String filePath, String originalName, String contentType,
      Long fileSizeBytes) {
    this.uploadUrl = uploadUrl;
    this.filePath = filePath;
    this.originalName = originalName;
    this.contentType = contentType;
    this.fileSizeBytes = fileSizeBytes;
  }
}
