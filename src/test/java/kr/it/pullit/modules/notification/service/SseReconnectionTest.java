package kr.it.pullit.modules.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import kr.it.pullit.modules.notification.api.NotificationPublicApi;
import kr.it.pullit.modules.notification.repository.EmitterRepository;
import kr.it.pullit.modules.questionset.web.dto.response.QuestionSetCreationCompleteResponse;
import kr.it.pullit.support.TestContainerTest;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

class SseReconnectionTest extends TestContainerTest {

  @Autowired
  private NotificationPublicApi notificationPublicApi;

  @Autowired
  private EmitterRepository emitterRepository;

  @LocalServerPort
  private int port;

  private static final Long TEST_USER_ID = 1L;
  private SseTestClient client;

  @BeforeEach
  void setUp() {
    client = new SseTestClient(port);
  }

  @AfterEach
  void tearDown() {
    client.shutdown();
  }

  @Test
  @DisplayName("Last-Event-ID 없이 재연결 시, 누락된 이벤트는 유실된다")
  void testEventLossOnReconnectionWithoutLastEventId() {
    // Given: A client connects and receives one event
    client.subscribe("/api/notifications/subscribe");
    await().atMost(4, TimeUnit.SECONDS).until(() -> client.getReceivedEvents().size() == 1);
    assertThat(client.getReceivedEvents().getFirst()).contains("EventStream Created");

    // When: The client disconnects
    client.disconnect();

    // And: We wait for the server to remove the emitter
    await().atMost(4, TimeUnit.SECONDS).until(() -> emitterRepository.notExistsById(TEST_USER_ID));

    // And: Two events are published while disconnected
    notificationPublicApi.publishQuestionSetCreationComplete(TEST_USER_ID,
        new QuestionSetCreationCompleteResponse(true, 101L, "New Question Set 1"));
    notificationPublicApi.publishQuestionSetCreationComplete(TEST_USER_ID,
        new QuestionSetCreationCompleteResponse(true, 102L, "New Question Set 2"));

    // And: The client reconnects WITHOUT Last-Event-ID
    client.subscribe("/api/notifications/subscribe");

    // Then: The client only receives the new connection event, and misses the 2 events.
    await().atMost(4, TimeUnit.SECONDS).until(() -> client.getReceivedEvents().stream()
        .filter(e -> e.contains("EventStream Created")).count() == 2);

    assertThat(client.getReceivedEvents()).hasSize(2);
    assertThat(client.getReceivedEvents().stream().filter(e -> e.contains("questionSetId\":101")))
        .isEmpty();
    assertThat(client.getReceivedEvents().stream().filter(e -> e.contains("questionSetId\":102")))
        .isEmpty();
  }

  @Test
  @DisplayName("Last-Event-ID와 함께 재연결 시, 누락된 이벤트를 모두 수신한다")
  void testNoEventLossOnReconnectionWithLastEventId() {
    // Given: A client connects and receives one event
    client.subscribe("/api/notifications/subscribe");
    await().atMost(4, TimeUnit.SECONDS).until(() -> client.getReceivedEvents().size() == 1);
    String firstEvent = client.getReceivedEvents().getFirst();
    assertThat(firstEvent).contains("EventStream Created");

    // When: The client disconnects
    client.disconnect();

    // And: We wait for the server to remove the emitter
    await().atMost(4, TimeUnit.SECONDS).until(() -> emitterRepository.notExistsById(TEST_USER_ID));

    // And: Two events are published while disconnected
    notificationPublicApi.publishQuestionSetCreationComplete(TEST_USER_ID,
        new QuestionSetCreationCompleteResponse(true, 101L, "New Question Set 1"));
    notificationPublicApi.publishQuestionSetCreationComplete(TEST_USER_ID,
        new QuestionSetCreationCompleteResponse(true, 102L, "New Question Set 2"));

    // And: The client reconnects WITH Last-Event-ID
    String lastEventId = parseIdFromEvent(firstEvent);
    client.subscribe("/api/notifications/subscribe?lastEventId=" + lastEventId);

    // Then: The client receives all 3 events (2 missed + 1 new connection)
    await().atMost(4, TimeUnit.SECONDS).until(() -> client.getReceivedEvents().size() == 4);

    assertThat(client.getReceivedEvents().stream().filter(e -> e.contains("questionSetId\":101")))
        .hasSize(1);
    assertThat(client.getReceivedEvents().stream().filter(e -> e.contains("questionSetId\":102")))
        .hasSize(1);
    assertThat(client.getReceivedEvents().stream().filter(e -> e.contains("EventStream Created")))
        .hasSize(2);
  }

  private String parseIdFromEvent(String eventString) {
    for (String line : eventString.split("\n")) {
      if (line.startsWith("id:")) {
        return line.substring(3).trim();
      }
    }
    return null;
  }

  // Helper class to simulate an SSE client
  private static class SseTestClient {
    private final int port;
    private final OkHttpClient client;
    private EventSource eventSource;
    @Getter
    private final List<String> receivedEvents = new CopyOnWriteArrayList<>();

    public SseTestClient(int port) {
      this.port = port;
      this.client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();
    }

    public void subscribe(String path) {
      Request request = new Request.Builder().url("http://localhost:" + port + path).get().build();

      eventSource =
          EventSources.createFactory(client).newEventSource(request, new EventSourceListener() {

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id,
                @Nullable String type, @NotNull String data) {
              // This is the correct implementation to capture events
              receivedEvents.add("id:" + id + "\n" + "event:" + type + "\n" + "data:" + data);
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
              // Connection closed
            }

            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t,
                @Nullable Response response) {
              // Handle failure
            }
          });
    }

    public void disconnect() {
      if (eventSource != null) {
        eventSource.cancel();
        eventSource = null;
      }
    }

    public void shutdown() {
      if (eventSource != null) {
        eventSource.cancel();
      }
      client.dispatcher().executorService().shutdown();
      client.connectionPool().evictAll();
    }
  }
}
