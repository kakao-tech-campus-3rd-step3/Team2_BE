package kr.it.pullit.modules.projection.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import kr.it.pullit.shared.jpa.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "outbox_event",
    indexes = {
      @Index(name = "ix_outbox_event_status_created", columnList = "status,createdAt"),
      @Index(name = "ix_outbox_event_type", columnList = "eventType")
    },
    uniqueConstraints =
        @UniqueConstraint(name = "uk_outbox_event_event_id", columnNames = "eventId"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

  public enum Status {
    PENDING,
    SENDING,
    DONE,
    FAILED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 36, nullable = false)
  private String eventId;

  @Column(length = 100, nullable = false)
  private String eventType; // WEEKLY_RESET, QUESTION_SOLVED, ...

  @Lob
  @Column(columnDefinition = "json", nullable = false)
  private String payload; // JSON 문자열

  @Enumerated(EnumType.STRING)
  @Column(length = 16, nullable = false)
  private Status status = Status.PENDING;

  private String workerId;
  private final Integer attempts = 0;

  @Builder(access = AccessLevel.PRIVATE)
  private OutboxEvent(String eventType, String payload) {
    this.eventId = UUID.randomUUID().toString();
    this.eventType = eventType;
    this.payload = payload;
  }

  public static OutboxEvent of(String eventType, String payload) {
    return OutboxEvent.builder().eventType(eventType).payload(payload).build();
  }
}
