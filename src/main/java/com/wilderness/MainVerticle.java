package com.wilderness;

import com.wilderness.io.ApiVerticle;
import com.wilderness.io.IotVerticle;
import com.wilderness.service.JdbcVerticle;
import com.wilderness.service.RedisVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {

        ClusterManager clusterManager = new HazelcastClusterManager();
        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);

        Vertx.clusteredVertx(options, res -> {
            if (res.succeeded()) {
                vertx = res.result();

                // 配置 RuntimeError 错误记录
                vertx.exceptionHandler(error -> log.error("未捕获的异常：", error));

                // 顺序部署 Verticle
                Future.<Void>succeededFuture()
                        .compose(v -> Future.<Void>future(s -> vertx.deployVerticle(new ApiVerticle(), new DeploymentOptions().setConfig(config()))))
                        .compose(v -> Future.<Void>future(s -> vertx.deployVerticle(new IotVerticle(), new DeploymentOptions().setConfig(config()))))
                        .compose(v -> Future.<Void>future(s -> vertx.deployVerticle(new JdbcVerticle(), new DeploymentOptions().setConfig(config()))))
                        .compose(v -> Future.<Void>future(s -> vertx.deployVerticle(new RedisVerticle(), new DeploymentOptions().setConfig(config()))))
                        .setHandler(result -> {
                            if (result.succeeded()) {
                                log.debug("Vert.x start succeeded");
                                startFuture.complete();
                            } else {
                                startFuture.fail(result.cause());
                            }
                        });
            } else {
                startFuture.fail(res.cause());
            }
        });
    }
}
