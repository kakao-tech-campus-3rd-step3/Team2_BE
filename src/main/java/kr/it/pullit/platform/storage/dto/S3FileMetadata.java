package kr.it.pullit.platform.storage.dto;

import java.time.Instant;
import lombok.Data;

@Data
public class S3FileMetadata {
  private final long contentLength;
  private final Instant lastModified;
}
