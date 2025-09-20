package kr.it.pullit.modules.learningsource.sourcefolder.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.it.pullit.modules.member.domain.entity.Member;
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

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false)
  private String name;

  private String description;

  private String color;

  @SuppressWarnings("unused")
  @Builder
  public SourceFolder(Member member, String name, String description, String color) {
    this.member = member;
    this.name = name;
    this.description = description;
    this.color = color;
  }
}
