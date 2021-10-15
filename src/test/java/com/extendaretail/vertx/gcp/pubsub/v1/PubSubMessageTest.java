package com.extendaretail.vertx.gcp.pubsub.v1;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;

class PubSubMessageTest {

  @Test
  @Timeout(2000)
  void thatConversionOk() {
    JsonObject jsonMessage = new JsonObject().put("testKey", "testValue");
    Map<String, String> attributes = new HashMap<>();
    attributes.put("attr1", "value1");
    JsonObject json =
        new JsonObject()
            .put("topic", "a-topic")
            .put("message", jsonMessage)
            .put("attributes", attributes);

    PubSubMessage message = new PubSubMessage(json);

    assertThat(message.getTopic()).isNotBlank().isEqualTo("a-topic");
    assertThat(message.getMessage()).isNotNull().isNotEmpty().isEqualTo(jsonMessage);
    assertThat(message.getAttributes()).isNotNull().isNotEmpty();
    assertThat(message.toJson()).isEqualTo(json);
  }
}
