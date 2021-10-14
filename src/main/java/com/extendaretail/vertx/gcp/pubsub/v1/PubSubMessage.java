package com.extendaretail.vertx.gcp.pubsub.v1;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;

/**
 * Value object to send a PubSub message via {@link PubSubService}
 *
 * @author thced
 */
@DataObject(generateConverter = true)
public class PubSubMessage {

  private String topic;
  private JsonObject message;

  public PubSubMessage() {
    this.topic = null;
    this.message = null;
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
}
