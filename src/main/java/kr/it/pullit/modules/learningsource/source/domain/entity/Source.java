package kr.it.pullit.modules.learningsource.source.domain.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import kr.it.pullit.modules.learningsource.source.constant.SourceStatus;
import kr.it.pullit.modules.learningsource.sourcefolder.domain.entity.SourceFolder;
import kr.it.pullit.modules.member.domain.entity.Member;
import kr.it.pullit.modules.questionset.domain.entity.QuestionSet;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Source extends BaseEntity {

  @ManyToMany(mappedBy = "sources")
  private final Set<QuestionSet> questionSets = new HashSet<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_folder_id")
  private SourceFolder sourceFolder;

  @Column(nullable = false)
  private String originalName;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private String filePath;

  @Column(nullable = false)
  private Long fileSizeBytes;

  private Integer pageCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SourceStatus status;

  @SuppressWarnings("unused")
  @Builder(access = AccessLevel.PRIVATE)
  private Source(Member member, SourceFolder sourceFolder, String originalName, String contentType,
      String filePath, Long fileSizeBytes, SourceStatus status) {
    this.member = member;
    this.sourceFolder = sourceFolder;
    this.originalName = originalName;
    this.contentType = contentType;
    this.filePath = filePath;
    this.fileSizeBytes = fileSizeBytes;
    this.status = status != null ? status : SourceStatus.UPLOADED;
  }

  public static Source create(SourceCreationParam param, Member member, SourceFolder sourceFolder) {
    return Source.builder().member(member).sourceFolder(sourceFolder)
        .originalName(param.originalName()).filePath(param.filePath())
        .contentType(param.contentType()).fileSizeBytes(param.fileSizeBytes())
        .status(SourceStatus.UPLOADED).build();
  }

  public LocalDateTime getRecentQuestionGeneratedAt() {
    return questionSets.stream().map(QuestionSet::getCreatedAt).max(LocalDateTime::compareTo)
        .orElse(null);
  }

  public void updateFileInfo(String originalName, String contentType, Long fileSizeBytes) {
    this.originalName = originalName;
    this.contentType = contentType;
    this.fileSizeBytes = fileSizeBytes;
    this.status = SourceStatus.UPLOADED;
  }

  public void startProcessing() {
    this.status = SourceStatus.PROCESSING;
  }

  public void markAsReady() {
    this.status = SourceStatus.READY;
  }

  public void markAsFailed() {
    this.status = SourceStatus.FAILED;
  }
}
