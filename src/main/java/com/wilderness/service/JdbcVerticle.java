package com.wilderness.service;

import com.wilderness.constants.Key;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
public class JdbcVerticle extends AbstractVerticle {

    private SQLConnection connection;

    @Override
    public void start(Promise<Void> startFuture) {

        JsonObject config = config();
        vertx.eventBus().consumer(Key.GET_IOTMACHINE_FROM_JDBC, this::getIotMachineFromJdbc);
        vertx.eventBus().consumer(Key.SET_HISTORY_TO_JDBC, this::setHistoryToJdbc);

        String jdbcUrl = config().getJsonObject("jdbc").getString("url", "jdbc:mysql://localhost:3306/local_test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&autoReconnectForPools=true&useSSL=true&createDatabaseIfNotExist=true&serverTimezone=UTC");
        String jdbcDriverClass = config().getJsonObject("jdbc").getString("driver_class", "com.mysql.cj.jdbc.Driver");
        String jdbcUser = config().getJsonObject("jdbc").getString("user", "root");
        String jdbcPassword = config().getJsonObject("jdbc").getString("password", "root");

        final JDBCClient client = JDBCClient.createShared(vertx, new JsonObject()
                .put("url", jdbcUrl)
                .put("driver_class", jdbcDriverClass)
                .put("user", jdbcUser)
                .put("password", jdbcPassword));

        client.getConnection(conn -> {
            if (conn.succeeded()) {
                log.debug("jdbc start succeeded");
                connection = conn.result();
                startFuture.complete();
            } else {
                startFuture.fail(conn.cause());
            }
        });
    }

    private void getIotMachineFromJdbc(Message<JsonObject> message) {
        log.debug("getIotMachineFromJdbc:" + System.currentTimeMillis());
        String userId = message.body().getString("userId");
        connection.query("select * from users where id = " + userId, query -> {
            if (query.failed()) {
                message.fail(500, query.cause().getMessage());
            } else {
                ResultSet result = query.result();
                if (result != null && query.result().getNumRows() > 0) {
                    message.reply(result.getRows().get(0));
                } else {
                    message.reply(new JsonObject());
                }
            }
        });
    }

    private void setHistoryToJdbc(Message<JsonObject> message) {
        log.debug("setHistoryToJdbc:" + System.currentTimeMillis());
        connection.query("select * from logs where data = '" + LocalDate.now() + "';", query -> {
            if (query.failed()) {
                message.fail(500, query.cause().getMessage());
            } else {
                ResultSet result = query.result();
                if (result != null && query.result().getNumRows() > 0) {
                    connection.execute("update logs set views = views + 1 where data = '" + LocalDate.now() + "';", query2 -> {
                        if (query2.failed()) {
                            message.fail(500, query2.cause().getMessage());
                        }
                    });
                } else {
                    connection.execute("insert into logs (`views`, `data`) VALUES (1, '" + LocalDate.now() + "');", query2 -> {
                        if (query2.failed()) {
                            message.fail(500, query2.cause().getMessage());
                        }
                    });
                }
            }
        });
    }
}
