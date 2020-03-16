package com.wilderness.api;

import com.wilderness.constants.Key;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author an_qiang
 */
@Slf4j
public class ApiVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // 全局日志处理，会执行 next() 到下一个
        router.get("/*").handler(this::log);
        router.get("/users/:userId").handler(this::handleGetUser);
        router.post("/users").handler(this::handleAddUser);
        router.get("/users").handler(this::handleListUsers);

        vertx.createHttpServer().requestHandler(router).listen(config().getInteger("http.port", 8080),
                result -> {
                    if (result.succeeded()) {
                        log.debug("api start succeeded");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                }
        );
    }

    private void log(RoutingContext routingContext) {
        vertx.eventBus().publish(Key.SET_HISTORY_TO_JDBC, null);
        vertx.eventBus().publish(Key.SET_HISTORY_TO_REDIS, null);
        routingContext.next();
    }

    private void handleGetUser(RoutingContext routingContext) {
        String userId = routingContext.request().getParam("userId");
        HttpServerResponse response = routingContext.response();
        JsonObject result = new JsonObject();
        if (userId == null) {
            sendError(400, response);
        } else {
            vertx.eventBus().<JsonObject>request(Key.GET_IOTMACHINE_FROM_JDBC, new JsonObject().put("userId", userId), res -> {
                if (res.succeeded()) {
                    result.put("user", res.result().body());
                    returnJsonWithCache(routingContext, result);
                } else {
                    routingContext.fail(res.cause());
                }
            });
        }
    }

    private void handleAddUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();


    }

    private void handleListUsers(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();

    }

    private void returnJsonWithCache(RoutingContext routingContext, JsonObject jsonObject) {
        routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(jsonObject.encodePrettily());
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }
}
