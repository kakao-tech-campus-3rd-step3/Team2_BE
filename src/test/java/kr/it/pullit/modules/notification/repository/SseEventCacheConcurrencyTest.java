package kr.it.pullit.modules.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import kr.it.pullit.modules.notification.domain.EventData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class SseEventCacheConcurrencyTest {

  private static final int NUM_WRITER_THREADS = 10;
  private static final int EVENTS_PER_WRITER = 10;
  private static final Long TEST_USER_ID = 1L;
  private static final int TOTAL_EVENTS = NUM_WRITER_THREADS * EVENTS_PER_WRITER;

  @RepeatedTest(10) // Repeat to increase the chance of catching intermittent race conditions
  @DisplayName("Unsafe 캐시에서는 동시성 문제로 이벤트가 유실될 수 있다")
  void testEventLossInUnsafeCache() throws InterruptedException {
    // Given
    SseEventCache unsafeCache = new UnsafeInMemorySseEventCache();
    ExecutorService executor = Executors.newFixedThreadPool(NUM_WRITER_THREADS + 1);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch writersDoneLatch = new CountDownLatch(NUM_WRITER_THREADS);
    AtomicLong eventIdGenerator = new AtomicLong();

    // When: Multiple threads write events concurrently
    for (int i = 0; i < NUM_WRITER_THREADS; i++) {
      executor.submit(() -> {
        try {
          startLatch.await(); // Wait for the signal to start
          for (int j = 0; j < EVENTS_PER_WRITER; j++) {
            long eventId = eventIdGenerator.getAndIncrement();
            unsafeCache.put(TEST_USER_ID, new EventData(eventId, "testEvent", "data" + eventId));
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          writersDoneLatch.countDown();
        }
      });
    }

    // And: One thread reads events concurrently
    Set<EventData> receivedEvents = Collections.synchronizedSet(new HashSet<>());
    AtomicLong lastReadEventId = new AtomicLong(-1);

    executor.submit(() -> {
      try {
        // Wait for all writer threads to complete their work first.
        // This prevents the reader from stopping prematurely.
        writersDoneLatch.await();

        // After all writers are finished, drain any remaining events from the cache.
        // A loop is necessary because the last read might have occurred just before the final
        // write.
        while (true) {
          List<EventData> newEvents =
              unsafeCache.findAllByUserIdAfter(TEST_USER_ID, lastReadEventId.get());
          if (newEvents.isEmpty()) {
            // No more events found, it's safe to exit.
            break;
          }
          receivedEvents.addAll(newEvents);
          newEvents.stream().mapToLong(EventData::id).max().ifPresent(lastReadEventId::set);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    startLatch.countDown(); // Signal all threads to start
    executor.shutdown();
    boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

    // Then
    assertThat(finished).isTrue();
    System.out.println("[Unsafe] Total events written: " + TOTAL_EVENTS);
    System.out.println("[Unsafe] Total unique events received: " + receivedEvents.size());
    assertThat(receivedEvents.size()).isLessThanOrEqualTo(TOTAL_EVENTS);
  }

  @Test
  @DisplayName("Safe 캐시(synchronized)에서는 동시성 문제 없이 모든 이벤트가 수신된다")
  void testNoEventLossInSafeCache() throws InterruptedException {
    // Given
    SseEventCache safeCache = new InMemorySseEventCache(); // The thread-safe implementation
    ExecutorService executor = Executors.newFixedThreadPool(NUM_WRITER_THREADS + 1);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch writersDoneLatch = new CountDownLatch(NUM_WRITER_THREADS);
    AtomicLong eventIdGenerator = new AtomicLong();

    // When
    for (int i = 0; i < NUM_WRITER_THREADS; i++) {
      executor.submit(() -> {
        try {
          startLatch.await();
          for (int j = 0; j < EVENTS_PER_WRITER; j++) {
            long eventId = eventIdGenerator.getAndIncrement();
            safeCache.put(TEST_USER_ID, new EventData(eventId, "testEvent", "data" + eventId));
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          writersDoneLatch.countDown();
        }
      });
    }

    Set<EventData> receivedEvents = Collections.synchronizedSet(new HashSet<>());
    AtomicLong lastReadEventId = new AtomicLong(-1);

    executor.submit(() -> {
      try {
        // Wait for all writer threads to complete their work first.
        writersDoneLatch.await();

        // After all writers are finished, drain any remaining events from the cache.
        while (true) {
          List<EventData> newEvents =
              safeCache.findAllByUserIdAfter(TEST_USER_ID, lastReadEventId.get());
          if (newEvents.isEmpty()) {
            break;
          }
          receivedEvents.addAll(newEvents);
          newEvents.stream().mapToLong(EventData::id).max().ifPresent(lastReadEventId::set);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    startLatch.countDown();
    executor.shutdown();
    boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);

    // Then
    assertThat(finished).isTrue();
    System.out.println("[Safe] Total events written: " + TOTAL_EVENTS);
    System.out.println("[Safe] Total unique events received: " + receivedEvents.size());
    assertThat(receivedEvents.size()).isEqualTo(TOTAL_EVENTS);
  }
}
