package kr.it.pullit.platform.migration.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationHistory extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String migrationName;

  private MigrationHistory(String migrationName) {
    this.migrationName = migrationName;
  }

  public static MigrationHistory create(String migrationName) {
    return new MigrationHistory(migrationName);
  }
}
