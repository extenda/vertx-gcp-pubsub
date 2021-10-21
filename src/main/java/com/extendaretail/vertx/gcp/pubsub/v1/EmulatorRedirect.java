package com.extendaretail.vertx.gcp.pubsub.v1;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher.Builder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Based on <a
 * href="https://cloud.google.com/pubsub/docs/emulator#pubsub-emulator-java">https://cloud.google.com/pubsub/docs/emulator#pubsub-emulator-java</a>
 *
 * <p>Supports running the library with an emulated pubsub service
 *
 * @author thced
 */
public class EmulatorRedirect {

  private EmulatorRedirect() {}

  public static void redirect(Builder builder) {
    String hostPort =
        System.getProperty("PUBSUB_EMULATOR_HOST", System.getenv("PUBSUB_EMULATOR_HOST"));
    ManagedChannel channel = ManagedChannelBuilder.forTarget(hostPort).usePlaintext().build();

    TransportChannelProvider channelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

    // Set the channel and credentials provider when creating a `Publisher`.
    builder.setChannelProvider(channelProvider).setCredentialsProvider(credentialsProvider);
  }
}
