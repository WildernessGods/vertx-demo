package com.wilderness;

import com.wilderness.io.IotClientVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Launcher {

    public static void main(String[] args) {

        VertxOptions vertxOptions = new VertxOptions();
        Vertx vertx = Vertx.vertx(vertxOptions);

//        ClusterManager clusterManager = new HazelcastClusterManager();
//        VertxOptions options = new VertxOptions().setClusterManager(clusterManager);
//
//        Vertx.clusteredVertx(options, res -> {
//            if (res.succeeded()) {
//                vertx = res.result();

        vertx.exceptionHandler(error -> log.error("未捕获的异常：", error));

        DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(1);
        vertx.deployVerticle(IotClientVerticle.class, deploymentOptions);

    }
}
