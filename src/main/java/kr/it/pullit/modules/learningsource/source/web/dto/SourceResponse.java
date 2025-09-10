package kr.it.pullit.modules.learningsource.source.web.dto;

import java.time.LocalDateTime;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.source.domain.entity.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SourceResponse {

  private Long id;
  private String originalName;
  private SourceStatus status;
  private Long fileSizeBytes;
  private LocalDateTime createdAt;

  public static SourceResponse from(Source source) {
    return SourceResponse.builder()
        .id(source.getId())
        .originalName(source.getOriginalName())
        .status(source.getStatus())
        .fileSizeBytes(source.getFileSizeBytes())
        .createdAt(source.getCreatedAt())
        .build();
  }
}

