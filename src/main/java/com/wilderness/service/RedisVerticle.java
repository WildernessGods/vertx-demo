package com.wilderness.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * @author an_qiang
 */
@Slf4j
public class RedisVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {
        startFuture.complete();
    }
}
