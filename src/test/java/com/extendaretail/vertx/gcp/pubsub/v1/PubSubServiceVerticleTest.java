package com.extendaretail.vertx.gcp.pubsub.v1;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.junit5.*;
import io.vertx.junit5.Timeout;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

@ExtendWith(VertxExtension.class)
class PubSubServiceVerticleTest {

  @Test
  @Timeout(2000)
  void thatDeploys(Vertx vertx, VertxTestContext testContext) { // NOSONAR
    vertx
        .deployVerticle(PubSubServiceVerticle::new, new DeploymentOptions())
        .onComplete(testContext.succeedingThenComplete());
  }
}
