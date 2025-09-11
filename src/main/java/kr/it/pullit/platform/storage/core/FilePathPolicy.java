package kr.it.pullit.platform.storage.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FilePathPolicy {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

  public String generateFilePath(String originalFileName, Long memberId) {
    String datePath = LocalDate.now().format(DATE_FORMAT);
    String fileExtension = extractFileExtension(originalFileName);
    String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

    return String.format("learning-sources/%s/member-%d/%s", datePath, memberId, uniqueFileName);
  }

  private String extractFileExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf("."));
  }
}
