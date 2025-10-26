package kr.it.pullit.modules.projection.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent extends BaseEntity {
  @Id
  @Column(length = 36)
  private String eventId;

  @Builder(access = AccessLevel.PRIVATE)
  public ProcessedEvent(String eventId) {
    this.eventId = eventId;
  }

  public static ProcessedEvent of(String eventId) {
    return ProcessedEvent.builder().eventId(eventId).build();
  }
}
