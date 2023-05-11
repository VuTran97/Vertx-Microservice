package org.example.service.impl;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
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
  public Handler<Message<Object>> getAll() {
    return userEventBus.getAll();
  }

  @Override
  public Handler<Message<JsonObject>> insert() {
    return userEventBus.insert();
  }

  @Override
  public Handler<Message<String>> getById() {
    return userEventBus.getById();
  }

  @Override
  public Handler<Message<JsonObject>> update() {
    return userEventBus.update();
  }

  @Override
  public Handler<Message<String>> delete() {
    return userEventBus.delete();
  }



}
