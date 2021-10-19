package com.extendaretail.vertx.gcp.pubsub.v1;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.Base64;

public class Base64DecodingHandler implements Handler<RoutingContext> {

  /**
   * The constant in the routing context, holding the decoded PubSub data (as {@link JsonObject})
   */
  public static final String CTX_DATA_KEY = "_pubsub_data";

  private static final JsonPointer data = JsonPointer.from("/message/data");

  @Override
  public void handle(RoutingContext ctx) {
    decode(ctx)
        .onSuccess(
            decodeString -> {
              if (nonNull(decodeString) && decodeString.length() > 0) {
                ctx.put(CTX_DATA_KEY, new JsonObject(decodeString));
              }
              ctx.next();
            })
        .onFailure(failureHandler(ctx));
  }

  private Future<String> decode(RoutingContext ctx) {
    return ctx.vertx()
        .executeBlocking(
            p -> {
              String encodedData = (String) data.queryJson(ctx.getBodyAsJson());
              p.complete(new String(Base64.getDecoder().decode(encodedData), UTF_8));
            },
            false);
  }

  private Handler<Throwable> failureHandler(RoutingContext ctx) {
    return throwable -> ctx.fail(new HttpException(BAD_REQUEST.code(), throwable));
  }
}
