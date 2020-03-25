package com.wilderness.io;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.net.NetServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author an_qiang
 */
@Slf4j
public class TcpVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {

        NetServer server = vertx.createNetServer();
        server.connectHandler(socket -> {
            socket.handler(buffer -> {
                System.out.println("I received some bytes: " + buffer.length());
            });
        });

    }
}
