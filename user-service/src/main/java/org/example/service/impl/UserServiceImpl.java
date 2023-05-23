package org.example.service.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.example.eventbus.UserEventBus;
import org.example.service.UserService;

public class UserServiceImpl implements UserService {

  private final UserEventBus userEventBus;

  public UserServiceImpl(UserEventBus userEventBus) {
      this.userEventBus = userEventBus;
    }

  @Override
  public Handler<Message<Object>> getAll(Vertx vertx) {
    return userEventBus.getAll(vertx);
  }



  @Override
  public Handler<Message<JsonObject>> insert(Vertx vertx) {
    return userEventBus.insert(vertx);
  }

  @Override
  public Handler<Message<String>> getById(Vertx vertx) {
    return userEventBus.getById(vertx);
  }

  @Override
  public Handler<Message<JsonObject>> update(Vertx vertx) {
    return userEventBus.update(vertx);
  }

  @Override
  public Handler<Message<String>> delete(Vertx vertx) {
    return userEventBus.delete(vertx);
  }



}
