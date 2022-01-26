package com.extendaretail.vertx.gcp.pubsub.v1;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import java.util.Map;

/**
 * Value object to send a PubSub message via {@link PubSubService}. While you can set both a Vertx
 * {@link JsonObject} and {@link Buffer} for the payload type, the {@link Buffer} takes precedence
 * and byte[] will only be used if no {@link Buffer} is set.
 *
 * @author thced
 */
@DataObject(generateConverter = true)
public class PubSubMessage {

  private String topic;
  private JsonObject message;
  private Buffer bufferMessage;
  private Map<String, String> attributes;

  public PubSubMessage() {
    this.topic = null;
    this.message = null;
    this.attributes = null;
    this.bufferMessage = null;
  }

  public PubSubMessage(JsonObject json) {
    PubSubMessageConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    PubSubMessageConverter.toJson(this, json);
    return json;
  }

  public String getTopic() {
    return topic;
  }

  @Fluent
  public PubSubMessage setTopic(String topic) {
    this.topic = topic;
    return this;
  }

  public JsonObject getMessage() {
    return message;
  }

  @Fluent
  public PubSubMessage setMessage(JsonObject message) {
    this.message = message;
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Fluent
  public PubSubMessage setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  public Buffer getBufferMessage() {
    return bufferMessage;
  }

  @Fluent
  public PubSubMessage setBufferMessage(Buffer bufferMessage) {
    this.bufferMessage = bufferMessage;
    return this;
  }
}
