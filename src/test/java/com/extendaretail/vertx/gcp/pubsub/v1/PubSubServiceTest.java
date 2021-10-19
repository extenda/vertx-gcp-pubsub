package com.extendaretail.vertx.gcp.pubsub.v1;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.Vertx;
import io.vertx.junit5.*;
import io.vertx.junit5.Timeout;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

@ExtendWith(VertxExtension.class)
class PubSubServiceTest {

  @Test
  @Timeout(2000)
  void thatProxyCreates(Vertx vertx, VertxTestContext testContext) {
    // Create service proxy
    PubSubService service = PubSubService.createProxy(vertx, "test-address");

    // Set up Eventbus mock
    vertx.eventBus().consumer("test-address", message -> message.reply(null));

    service
        .publish(new PubSubMessage())
        .onComplete(ar -> testContext.verify(() -> assertThat(ar.succeeded()).isTrue()))
        .onComplete(testContext.succeedingThenComplete());
  }
}
