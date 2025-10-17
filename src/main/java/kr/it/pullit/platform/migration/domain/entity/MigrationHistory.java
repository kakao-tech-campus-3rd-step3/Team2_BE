package kr.it.pullit.platform.migration.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MigrationHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String migrationName;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime completedAt;

  private MigrationHistory(String migrationName) {
    this.migrationName = migrationName;
    this.completedAt = LocalDateTime.now();
  }

  public static MigrationHistory create(String migrationName) {
    return new MigrationHistory(migrationName);
  }
}
