package com.extendaretail.vertx.gcp.pubsub.v1;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * Verticle that expose the {@link PubSubService} on the Vert.x eventbus
 *
 * @author thced
 */
public final class PubSubServiceVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    new ServiceBinder(vertx)
        .setAddress(PubSubService.address())
        .setIncludeDebugInfo(config().getBoolean("includeDebugInfo", true))
        .register(PubSubService.class, PubSubService.create(vertx))
        .completionHandler(startPromise);
  }
}
