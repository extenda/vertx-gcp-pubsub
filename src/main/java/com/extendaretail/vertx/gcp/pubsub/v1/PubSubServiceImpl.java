package com.extendaretail.vertx.gcp.pubsub.v1;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PubsubMessage.Builder;
import com.google.pubsub.v1.TopicName;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.Objects;


/**
 * Implementation of the PubSubService client code.
 *
 * @author thced
 */
/* protected */ class PubSubServiceImpl implements PubSubService {

  private static final String PROJECT_ID = "SERVICE_PROJECT_ID";

  private final Vertx vertx;
  private final Map<String, Publisher> publishers = new ConcurrentHashMap<>();

  public PubSubServiceImpl(Vertx vertx) throws ServiceException {
    this.vertx = vertx;
  }

  @Override
  public Future<Void> publish(PubSubMessage message) {
    return vertx.executeBlocking(publishInternal(message));
  }

  private Handler<Promise<Void>> publishInternal(PubSubMessage message) {
    return p -> {
      try {
        JsonObject jsonPayload = message.getMessage();
        Buffer buffer = jsonPayload == null ? message.getBufferMessage() : jsonPayload.toBuffer();
        String topic = message.getTopic();
        Map<String, String> attributes = message.getAttributes();
        PubsubMessage pubsubMessage = newPubSubMessage(attributes, bufferToByteString(buffer));
        ApiFuture<String> messageFuture = getPublisher(topic).publish(pubsubMessage);

        ApiFutures.addCallback(messageFuture, new InternalCallback(p), getExecutor());
      } catch (Exception e) {
        p.fail(e);
      }
    };
  }

  private Publisher getPublisher(String topic) throws IOException {
    if (publishers.containsKey(topic)) {
      return publishers.get(topic);
    } else {
      Publisher publisher = createPublisher(topic);
      publishers.put(topic, publisher);
      return publisher;
    }
  }

  private Publisher createPublisher(String topic) throws IOException {
    TopicName topicName = getTopicName(topic);
    Publisher.Builder builder = Publisher.newBuilder(topicName);
    if (System.getProperty("PUBSUB_EMULATOR_HOST", System.getenv("PUBSUB_EMULATOR_HOST")) != null) {
      EmulatorRedirect.redirect(builder);
    }
    return builder.build();
  }

  private PubsubMessage newPubSubMessage(Map<String, String> attributes, ByteString data) {
    Builder builder = PubsubMessage.newBuilder();
    if (attributes != null && !attributes.isEmpty()) {
      builder.putAllAttributes(attributes);
    }
    return builder.setData(data).build();
  }

  private ByteString bufferToByteString(Buffer buffer) {
    return ByteString.copyFrom(buffer.getBytes());
  }

  private Executor getExecutor() {
    return ((VertxInternal) vertx).getWorkerPool().executor();
  }

  private TopicName getTopicName(String topic) {
    String project = System.getProperty(PROJECT_ID, System.getenv(PROJECT_ID));

    // Check if project is empty or null
    Objects.requireNonNull(project, "PROJECT_ID has to be set either as a System variable or Environment variable");
    
    return TopicName.of(project, topic);
  }

  /** Used in tests to be able to mock publisher(s) */
  protected Map<String, Publisher> getPublishers() {
    return publishers;
  }

  /** Encapsulating class to map callbacks to the success or failure of the Promise. */
  private static class InternalCallback implements ApiFutureCallback<String> {

    private final Promise<Void> completionPromise;

    public InternalCallback(Promise<Void> promise) {
      this.completionPromise = promise;
    }

    @Override
    public void onFailure(Throwable throwable) {
      completionPromise.tryFail(throwable);
    }

    @Override
    public void onSuccess(String id) {
      completionPromise.tryComplete();
    }
  }
}
