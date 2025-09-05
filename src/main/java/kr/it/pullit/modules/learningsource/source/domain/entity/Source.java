package kr.it.pullit.modules.learningsource.source.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Source extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private String originalName;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private String filePath;

  @Column(nullable = false)
  private Long fileSizeBytes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SourceStatus status;

  @Builder
  public Source(Long memberId, String originalName, String contentType, String filePath,
      Long fileSizeBytes, SourceStatus status) {
    this.memberId = memberId;
    this.originalName = originalName;
    this.contentType = contentType;
    this.filePath = filePath;
    this.fileSizeBytes = fileSizeBytes;
    this.status = status != null ? status : SourceStatus.UPLOADED;
  }


}
