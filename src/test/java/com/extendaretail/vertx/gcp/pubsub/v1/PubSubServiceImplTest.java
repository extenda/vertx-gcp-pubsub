package com.extendaretail.vertx.gcp.pubsub.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.*;
import io.vertx.junit5.Timeout;
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
  void testFailureWithoutPubSubConfigPresent(
      Vertx vertx, VertxTestContext testContext, Tooling tooling) {
    PubSubServiceImpl service = new PubSubServiceImpl(vertx);

    PubSubMessage message =
        new PubSubMessage().setTopic(tooling.getTopicId()).setMessage(new JsonObject());

    service.publish(message).onComplete(testContext.failingThenComplete());
  }

  @Test
  @Timeout(5000)
  void testEmulatorPublisherCreation(Vertx vertx, VertxTestContext testContext, Tooling tooling) {
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
