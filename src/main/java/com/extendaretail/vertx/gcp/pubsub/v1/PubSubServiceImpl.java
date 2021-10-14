package com.extendaretail.vertx.gcp.pubsub.v1;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

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
        JsonObject payload = message.getMessage();
        String topic = message.getTopic();
        PubsubMessage pubsubMessage = newPubSubMessage(jsonObjectToByteString(payload));
        ApiFuture<String> messageFuture = getPublisher(topic).publish(pubsubMessage);

        ApiFutures.addCallback(messageFuture, new PubSubCallback(p), getExecutor());
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
    return Publisher.newBuilder(topicName).build();
  }

  private PubsubMessage newPubSubMessage(ByteString data) {
    return PubsubMessage.newBuilder().setData(data).build();
  }

  private ByteString jsonObjectToByteString(JsonObject jsonObject) {
    return ByteString.copyFrom(jsonObject.toBuffer().getBytes());
  }

  private Executor getExecutor() {
    return ((VertxImpl) vertx).getWorkerPool();
  }

  private TopicName getTopicName(String topic) {
    String project = System.getProperty(PROJECT_ID, System.getenv(PROJECT_ID));
    return TopicName.of(project, topic);
  }

  /** Used in tests to be able to mock publisher(s) */
  protected Map<String, Publisher> getPublishers() {
    return publishers;
  }

  /** Encapsulating class to map callbacks to the success or failure of the Promise. */
  private static class PubSubCallback implements ApiFutureCallback<String> {

    private final Promise<Void> completionPromise;

    public PubSubCallback(Promise<Void> promise) {
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