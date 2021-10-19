package com.extendaretail.vertx.gcp.pubsub.v1;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceException;

/**
 * Service interface for communicating with PubSub
 *
 * @author thced
 */
@ProxyGen
public interface PubSubService {

  /**
   * Convenience method to rely on the versioned package name as eventbus address.
   *
   * @return The EventBus address for this service
   */
  static String address() {
    return PubSubService.class.getName();
  }

  /**
   * Create a service instance. This should only be called by {@link PubSubServiceVerticle} or
   * similar class.
   *
   * @param vertx The Vert.x instance
   * @return The service instance
   * @throws ServiceException containing the error about why the instance failed to start
   */
  static PubSubService create(Vertx vertx) throws ServiceException {
    return new PubSubServiceImpl(vertx);
  }

  /**
   * Create a proxied service reference, that communicates of the EventBus
   *
   * @param vertx The Vert.x instance
   * @param address The eventbus address to use
   * @return The proxy service reference
   */
  static PubSubService createProxy(Vertx vertx, String address) {
    return new PubSubServiceVertxEBProxy(vertx, address);
  }

  /**
   * Publish a message on PubSub
   *
   * @param message The message to send
   * @return Future containing the result, or a failure
   */
  Future<Void> publish(PubSubMessage message);
}
