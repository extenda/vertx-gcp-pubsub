package com.extendaretail.vertx.gcp.pubsub.v1;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;

/**
 * Add as test method parameter to get references to the publisher and the subscriber, within tests.
 *
 * @author thced
 * @see PubSubContainerExtension
 */
public class Tooling {
  private final Publisher publisher;
  private final String topicId;
  private final SubscriberStub subscriber;
  private final String subscriptionId;
  private final String projectId;
  private final String hostPort;

  protected Tooling(
      Publisher publisher,
      String topicId,
      SubscriberStub subscriber,
      String subscriptionId,
      String projectId,
      String hostPort) {
    this.publisher = publisher;
    this.topicId = topicId;
    this.subscriber = subscriber;
    this.subscriptionId = subscriptionId;
    this.projectId = projectId;
    this.hostPort = hostPort;
  }

  public Publisher getPublisher() {
    return publisher;
  }

  public String getTopicId() {
    return topicId;
  }

  public SubscriberStub getSubscriber() {
    return subscriber;
  }

  public String getSubscriptionId() {
    return subscriptionId;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getHostPort() {
    return hostPort;
  }

  public ReceivedMessage receiveLastMessage() {
    PullRequest pullRequest =
        PullRequest.newBuilder()
            .setMaxMessages(1)
            .setSubscription(ProjectSubscriptionName.format(projectId, subscriptionId))
            .build();
    PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);
    return pullResponse.getReceivedMessages(0);
  }
}
