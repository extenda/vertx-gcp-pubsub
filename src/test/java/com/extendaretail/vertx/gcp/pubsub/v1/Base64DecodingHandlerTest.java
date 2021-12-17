package com.extendaretail.vertx.gcp.pubsub.v1;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.*;
import io.vertx.junit5.Timeout;
import java.util.Base64;
import java.util.Base64.Encoder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;

@DisplayName("Base64DecodingHandler")
@ExtendWith(VertxExtension.class)
class Base64DecodingHandlerTest {

  Encoder encoder = Base64.getEncoder();

  /** The path to where PubSub store data contents */
  JsonPointer data = JsonPointer.from("/message/data");

  JsonObject testJson = new JsonObject();

  JsonObject expectedResult = new JsonObject().put("testKey", "testValue");

  private Router router;
  private int port = 0;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(0)
        .onSuccess(server -> port = server.actualPort())
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(5000)
  @DisplayName("Should be able to decode payload from PubSub message")
  void testPayloadDecode(Vertx vertx, VertxTestContext testContext) {
    String pubSubData = new JsonObject().put("testKey", "testValue").encode();
    String encoded = encoder.encodeToString(pubSubData.getBytes());
    data.writeJson(testJson, encoded, true);

    Route route =
        router
            .post("/")
            .handler(new Base64DecodingHandler())
            .handler(
                ctx -> {
                  JsonObject data = ctx.get(Base64DecodingHandler.CTX_DATA_KEY);
                  testContext.verify(() -> assertThat(data).isEqualTo(expectedResult));
                  ctx.end();
                });

    WebClient.create(vertx)
        .post(port, "localhost", "/")
        .expect(ResponsePredicate.SC_OK)
        .sendJsonObject(testJson)
        .onComplete(ignore -> route.remove())
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  @Timeout(5000)
  @DisplayName("Should fail to decode payload from PubSub message")
  void testBadPayloadDecode(Vertx vertx, VertxTestContext testContext) {
    JsonObject pubSubData = new JsonObject().put("testKey", "testValue");

    Route route =
        router
            .post("/")
            .handler(new Base64DecodingHandler())
            .handler(RoutingContext::end)
            .failureHandler(ctx -> ctx.response().setStatusCode(400).end());

    WebClient.create(vertx)
        .post(port, "localhost", "/")
        .expect(ResponsePredicate.SC_BAD_REQUEST)
        .sendJsonObject(pubSubData)
        .onComplete(ignore -> route.remove())
        .onComplete(testContext.succeedingThenComplete());
  }
}
