package kr.it.pullit.modules.learningsource.source.web.dto;

import lombok.Getter;

@Getter
public class UploadResponse {

  private final String uploadUrl;
  private final String filePath;

  public UploadResponse(String uploadUrl, String filePath) {
    this.uploadUrl = uploadUrl;
    this.filePath = filePath;
  }
}
