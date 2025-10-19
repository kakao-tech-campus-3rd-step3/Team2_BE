package kr.it.pullit.modules.learningsource.sourcefolder.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SourceFolder extends BaseEntity {

  public static final String DEFAULT_FOLDER_NAME = "전체 폴더";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "member_id", nullable = false)
  private Long memberId;

  @Column(nullable = false)
  private String name;

  private String description;

  private String color;

  @SuppressWarnings("unused")
  @Builder(access = AccessLevel.PRIVATE)
  public SourceFolder(Long memberId, String name, String description, String color) {
    this.memberId = memberId;
    this.name = name;
    this.description = description;
    this.color = color;
  }

  public static SourceFolder create(Long memberId, String name, String description, String color) {
    return SourceFolder.builder()
        .memberId(memberId)
        .name(name)
        .description(description)
        .color(color)
        .build();
  }

  public static SourceFolder createDefaultFolder(Long memberId) {
    return SourceFolder.builder().memberId(memberId).name(DEFAULT_FOLDER_NAME).build();
  }
}
