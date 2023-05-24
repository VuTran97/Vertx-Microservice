package org.example.service;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;

public interface UserService {

    Handler<Message<Object>> getAll(Vertx vertx);

    Handler<Message<JsonObject>> insert(Vertx vertx);

    Handler<Message<String>> getById(Vertx vertx);

    Handler<Message<JsonObject>> update(Vertx vertx);

    Handler<Message<String>> delete(Vertx vertx);
}
