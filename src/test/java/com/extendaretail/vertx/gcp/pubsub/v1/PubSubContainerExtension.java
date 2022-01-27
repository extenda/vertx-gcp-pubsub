package com.extendaretail.vertx.gcp.pubsub.v1;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * JUnit 5 extension that enables us to easily inject a PubSub emulating container
 *
 * @author thced
 */
@Testcontainers
public class PubSubContainerExtension
    implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

  public static final String PROJECT_ID = "test-project";

  @Container
  private final PubSubEmulatorContainer emulator =
      new PubSubEmulatorContainer(
          DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:316.0.0-emulators"));

  @Override
  public void beforeAll(ExtensionContext extensionContext) {
    emulator.start();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    emulator.stop();
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(Tooling.class);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    String hostPort = emulator.getEmulatorEndpoint();
    ManagedChannel channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();
    TransportChannelProvider channelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    NoCredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    Publisher publisher = null;
    SubscriberStub subscriber = null;

    String topicId = generateRandomTopicId();
    String subscriptionId = generateRandomSubscriptionId();

    try {
      createTopic(topicId, channelProvider, credentialsProvider);
      createSubscription(subscriptionId, topicId, channelProvider, credentialsProvider);

      publisher =
          Publisher.newBuilder(TopicName.of(PROJECT_ID, topicId))
              .setChannelProvider(channelProvider)
              .setCredentialsProvider(credentialsProvider)
              .build();

      SubscriberStubSettings subscriberStubSettings =
          SubscriberStubSettings.newBuilder()
              .setTransportChannelProvider(channelProvider)
              .setCredentialsProvider(credentialsProvider)
              .build();

      subscriber = GrpcSubscriberStub.create(subscriberStubSettings);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return new Tooling(publisher, topicId, subscriber, subscriptionId, PROJECT_ID, hostPort);
  }

  private String generateRandomSubscriptionId() {
    return "testSubscription" + randomAlphanumericString();
  }

  private String generateRandomTopicId() {
    return "testTopic" + randomAlphanumericString();
  }

  private String randomAlphanumericString() {
    return UUID.randomUUID().toString().split("-")[0];
  }

  private void createTopic(
      String topicId,
      TransportChannelProvider channelProvider,
      NoCredentialsProvider credentialsProvider)
      throws IOException {
    TopicAdminSettings topicAdminSettings =
        TopicAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build();
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminSettings)) {
      TopicName topicName = TopicName.of(PROJECT_ID, topicId);
      topicAdminClient.createTopic(topicName);
    } catch (AlreadyExistsException e) {
      // The topic already exists -- OK
    }
  }

  private void createSubscription(
      String subscriptionId,
      String topicId,
      TransportChannelProvider channelProvider,
      NoCredentialsProvider credentialsProvider)
      throws IOException {
    SubscriptionAdminSettings subscriptionAdminSettings =
        SubscriptionAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build();
    SubscriptionAdminClient subscriptionAdminClient =
        SubscriptionAdminClient.create(subscriptionAdminSettings);

    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(PROJECT_ID, subscriptionId);

    try {
      subscriptionAdminClient.createSubscription(
          subscriptionName, TopicName.of(PROJECT_ID, topicId), PushConfig.getDefaultInstance(), 10);
    } catch (AlreadyExistsException e) {
      // The subscription already exists -- OK
    }
  }
}
