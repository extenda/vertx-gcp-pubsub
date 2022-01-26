package com.extendaretail.vertx.gcp.pubsub.v1;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ReceivedMessage;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.*;
import io.vertx.junit5.Timeout;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({VertxExtension.class, PubSubContainerExtension.class, MockitoExtension.class})
class PubSubServiceImplTest {

  @Test
  @Timeout(5000)
  void testProxyCall(Vertx vertx, VertxTestContext testContext, Tooling tooling) { // NOSONAR
    PubSubServiceImpl service = new PubSubServiceImpl(vertx);
    service.getPublishers().put(tooling.getTopicId(), tooling.getPublisher());

    PubSubMessage message =
        new PubSubMessage().setTopic(tooling.getTopicId()).setMessage(new JsonObject());

    service.publish(message).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(5000)
  void testJsonObjectDataShouldTakePrecedenceOverBufferMessage(
      Vertx vertx, VertxTestContext testContext, Tooling tooling) { // NOSONAR
    PubSubServiceImpl service = new PubSubServiceImpl(vertx);
    service.getPublishers().put(tooling.getTopicId(), tooling.getPublisher());

    PubSubMessage message =
        new PubSubMessage()
            .setTopic(tooling.getTopicId())
            .setMessage(new JsonObject("{\"message\":\"Hello World\"}"))
            .setBufferMessage(Buffer.buffer("ByteArrayMessage".getBytes(StandardCharsets.UTF_8)));

    service.publish(message).onComplete(testContext.succeedingThenComplete());

    List<ReceivedMessage> receivedMessage = tooling.receiveMessages(1);

    assertThat(receivedMessage.get(0).getMessage().getData().toStringUtf8())
        .isEqualTo("{\"message\":\"Hello World\"}");
  }

  @Test
  @Timeout(5000)
  void testBufferMessageSending(
      Vertx vertx, VertxTestContext testContext, Tooling tooling) { // NOSONAR
    PubSubServiceImpl service = new PubSubServiceImpl(vertx);
    service.getPublishers().put(tooling.getTopicId(), tooling.getPublisher());

    PubSubMessage message =
        new PubSubMessage()
            .setTopic(tooling.getTopicId())
            .setBufferMessage(Buffer.buffer("ByteArrayMessage".getBytes(StandardCharsets.UTF_8)));

    service.publish(message).onComplete(testContext.succeedingThenComplete());

    List<ReceivedMessage> receivedMessage = tooling.receiveMessages(1);

    assertThat(receivedMessage.get(0).getMessage().getData().toStringUtf8())
        .isEqualTo("ByteArrayMessage");
  }

  @Test
  @Timeout(5000)
  void testFailureWithoutPubSubConfigPresent(
      Vertx vertx, VertxTestContext testContext, Tooling tooling) {
    PubSubServiceImpl service = new PubSubServiceImpl(vertx);

    PubSubMessage message =
        new PubSubMessage().setTopic(tooling.getTopicId()).setMessage(new JsonObject());

    service.publish(message).onComplete(testContext.failingThenComplete());
  }

  @Test
  @Timeout(5000)
  void testEmulatorPublisherCreation( // NOSONAR
      Vertx vertx, VertxTestContext testContext, Tooling tooling) {
    System.setProperty("PUBSUB_EMULATOR_HOST", tooling.getHostPort());
    System.setProperty("SERVICE_PROJECT_ID", tooling.getProjectId());

    PubSubServiceImpl service = new PubSubServiceImpl(vertx);

    PubSubMessage message =
        new PubSubMessage().setTopic(tooling.getTopicId()).setMessage(new JsonObject());

    service
        .publish(message)
        .onComplete(
            ignore -> {
              System.clearProperty("PUBSUB_EMULATOR_HOST");
              System.clearProperty("SERVICE_PROJECT_ID");
            })
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(5000)
  void testFailedPubSubMessage(
      Vertx vertx, VertxTestContext testContext, @Mock Publisher publisher) {
    Throwable exception = new RuntimeException("expected from test");
    lenient()
        .when(publisher.publish(any()))
        .thenReturn(ApiFutures.immediateFailedFuture(exception));

    PubSubServiceImpl service = new PubSubServiceImpl(vertx);
    service.getPublishers().put("a-topic", publisher);

    service
        .publish(new PubSubMessage().setTopic("a-topic"))
        .onComplete(testContext.failingThenComplete());
  }
}
