package kr.it.pullit.modules.commonfolder.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.it.pullit.modules.commonfolder.domain.enums.CommonFolderType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "common_folder")
public class CommonFolder {

  public static final String DEFAULT_NAME = "전체";
  public static final Long DEFAULT_FOLDER_ID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CommonFolderType type;

  @Column(nullable = false)
  private int sortOrder;

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @Builder(access = AccessLevel.PRIVATE)
  public CommonFolder(String name, CommonFolderType type, int sortOrder, Long ownerId) {
    this.name = name;
    this.type = type;
    this.sortOrder = sortOrder;
    this.ownerId = ownerId;
  }

  public static CommonFolder create(
      String name, CommonFolderType type, int sortOrder, Long ownerId) {
    return CommonFolder.builder()
        .name(name)
        .type(type)
        .sortOrder(sortOrder)
        .ownerId(ownerId)
        .build();
  }

  public void update(String name) {
    this.name = name;
  }

  public void updateSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }
}
