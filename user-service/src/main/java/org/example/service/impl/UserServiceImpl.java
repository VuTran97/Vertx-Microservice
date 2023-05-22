package org.example.service.impl;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.example.converter.UserConverter;
import org.example.eventbus.UserEventBus;
import org.example.service.UserService;

public class UserServiceImpl implements UserService {

  private final UserEventBus userEventBus;

  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  private UserConverter userConverter = new UserConverter();

    public UserServiceImpl(UserEventBus userEventBus) {
      this.userEventBus = userEventBus;
    }

  @Override
  public Handler<Message<Object>> getAll(Vertx vertx) {
    return userEventBus.getAll(vertx);
  }



  @Override
  public Handler<Message<JsonObject>> insert(Vertx vertx, KafkaConsumer<String, String> consumer) {
      listeningTopic(consumer);
    return userEventBus.insert(vertx);
  }

  private void listeningTopic(KafkaConsumer<String, String> consumer){
      consumer.subscribe("user-topic1");
    consumer.handler(record -> {
      logger.info("Processing: key={0}, value={1}, partition={2}, offset={3}", record.key(), record.value(), record.partition(), record.offset());
    });
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
