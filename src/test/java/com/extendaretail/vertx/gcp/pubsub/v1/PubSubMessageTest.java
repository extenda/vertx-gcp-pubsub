package com.extendaretail.vertx.gcp.pubsub.v1;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import org.junit.jupiter.api.*;

class PubSubMessageTest {

  @Test
  @Timeout(2000)
  void thatConversionOk() {
    JsonObject jsonMessage = new JsonObject().put("testKey", "testValue");
    JsonObject json = new JsonObject().put("topic", "a-topic").put("message", jsonMessage);

    PubSubMessage message = new PubSubMessage(json);

    assertThat(message.getTopic()).isNotBlank().isEqualTo("a-topic");
    assertThat(message.getMessage()).isNotNull().isNotEmpty().isEqualTo(jsonMessage);
    assertThat(message.toJson()).isEqualTo(json);
  }
}
