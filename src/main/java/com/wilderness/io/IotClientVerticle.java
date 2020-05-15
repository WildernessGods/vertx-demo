
package com.wilderness.io;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * An example of using the MQTT client
 */
@Slf4j
public class IotClientVerticle extends AbstractVerticle {

    private static final String MQTT_TOPIC = "Shell";
    private static final String MQTT_MESSAGE = "Hello Vert.x MQTT Client";
    private static final String BROKER_HOST = "localhost";
    private static final int BROKER_PORT = 1883;

    @Override
    public void start() throws Exception {
        MqttClientOptions options = new MqttClientOptions().setKeepAliveTimeSeconds(2);

        MqttClient client = MqttClient.create(Vertx.vertx(), options);

        // handler will be called when we have a message in topic we subscribing for
        client.publishHandler(publish -> {
            log.debug("Just received message on [" + publish.topicName() + "] payload [" + publish.payload().toString(Charset.defaultCharset()) + "] with QoS [" + publish.qosLevel() + "]");

            if (publish.topicName().equals("Shell2")) {
                client.publish("Shell", Buffer.buffer(MQTT_MESSAGE), MqttQoS.AT_MOST_ONCE, false, false, s -> log.debug("Publish sent to a server"));
            }
        });

        client.publishCompletionHandler(publishCompletionHandler -> log.debug("publish completion " + publishCompletionHandler.toString()));

        // handle response on subscribe request
        client.subscribeCompletionHandler(h -> {
            log.debug("Receive SUBACK from server with granted QoS : " + h.grantedQoSLevels());
//            vertx.setTimer(5000, l -> client.unsubscribe(MQTT_TOPIC));
        });


        // handle response on unsubscribe request
//        client.unsubscribeCompletionHandler(h -> {
//            log.debug("Receive UNSUBACK from server");
//            vertx.setTimer(5000, l ->
//                    // disconnect for server
//                    client.disconnect(d -> log.debug("Disconnected form server"))
//            );
//        });

        // connect to a server
        client.connect(BROKER_PORT, BROKER_HOST, ch -> {
            if (ch.succeeded()) {
                log.debug("connected to mqtt server");
                vertx.setTimer(5000, l -> client.publish("Shell/Washer/CMD/", Buffer.buffer("199002"), MqttQoS.AT_MOST_ONCE, false, false, s -> log.debug("Publish sent to a server")));
            } else {
                log.error(ch.cause().getLocalizedMessage());
            }
        });
    }
}
