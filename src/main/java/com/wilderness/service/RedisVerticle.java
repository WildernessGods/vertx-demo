package com.wilderness.service;

import com.wilderness.constants.Key;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

/**
 * @author an_qiang
 */
@Slf4j
public class RedisVerticle extends AbstractVerticle {

    private RedisClient redisClient;

    @Override
    public void start(Promise<Void> startFuture) {

        vertx.eventBus().consumer(Key.SET_HISTORY_TO_REDIS, this::setHistoryToRedis);

        RedisOptions redisOptions = new RedisOptions()
                .setHost(config().getJsonObject("redis").getString("host", "127.0.0.1"))
                .setPort(config().getJsonObject("redis").getInteger("port", 6379))
                .setSelect(config().getJsonObject("redis").getInteger("select", 0));

        redisClient = RedisClient.create(vertx, redisOptions);

        startFuture.complete();
    }

    private void setHistoryToRedis(Message<JsonObject> message) {
        redisClient.hincrby(Key.REDIS_CLICKS_HISTORY_HASH, LocalDate.now().toString(), 1, res -> {
            if (res.failed()) {
                log.error("Fail to get data from Redis", res.cause());
            }
        });
        redisClient.hincrby(Key.REDIS_CLICKS_TOTAL_HASH, "total", 1, res -> {
            if (res.failed()) {
                log.error("Fail to get data from Redis", res.cause());
            }
        });
    }
}
