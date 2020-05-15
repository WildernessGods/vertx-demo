package com.wilderness.io;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.mqtt.MqttServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author an_qiang
 */
@Slf4j
public class IotServerVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {

        MqttServer mqttServer = MqttServer.create(vertx);
        mqttServer.endpointHandler(endpoint -> {

            log.debug("connected client " + endpoint.clientIdentifier());

            endpoint.publishHandler(message -> {
                log.debug("Just received message on [" + message.topicName() + "] payload [" +
                        message.payload() + "] with QoS [" +
                        message.qosLevel() + "]");
            });

            endpoint.accept(true);
        }).listen(config().getInteger("iot.port", 9000), ar -> {
            if (ar.succeeded()) {
                log.debug("MQTT server start succeeded");
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }
}
