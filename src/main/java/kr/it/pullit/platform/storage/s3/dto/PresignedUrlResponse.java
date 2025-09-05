package kr.it.pullit.platform.storage.s3.dto;

public class PresignedUrlResponse {
  private final String uploadUrl;
  private final String filePath;

  public PresignedUrlResponse(String uploadUrl, String filePath) {
    this.uploadUrl = uploadUrl;
    this.filePath = filePath;
  }

  public String getUploadUrl() {
    return uploadUrl;
  }

  public String getFilePath() {
    return filePath;
  }
}