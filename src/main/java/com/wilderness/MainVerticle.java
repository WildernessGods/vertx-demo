package com.wilderness;

import com.wilderness.api.ApiVerticle;
import com.wilderness.service.JdbcVerticle;
import com.wilderness.service.RedisVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {
        // 配置 RuntimeError 错误记录
        vertx.exceptionHandler(error -> log.error("未捕获的异常：", error));

        // 顺序部署 Verticle
        Future.<Void>succeededFuture()
                .compose(v -> Future.<String>future(s -> vertx.deployVerticle(new ApiVerticle(), new DeploymentOptions().setConfig(config()), s)))
                .compose(v -> Future.<String>future(s -> vertx.deployVerticle(new JdbcVerticle(), new DeploymentOptions().setConfig(config()), s)))
                .compose(v -> Future.<String>future(s -> vertx.deployVerticle(new RedisVerticle(), new DeploymentOptions().setConfig(config()), s)))
                .setHandler(result -> {
                    if (result.succeeded()) {
                        log.debug("Vert.x start succeeded");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }
}
