package org.example.service.impl;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.example.converter.UserConverter;
import org.example.repository.UserRepository;
import org.example.service.UserService;

public class UserServiceImpl implements UserService {


  private final UserRepository userRepository;

  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  private UserConverter userConverter = new UserConverter();

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }



  @Override
  public Handler<Message<Object>> getAllUserEventBus() {
    return userRepository.getAllUser();
  }

  @Override
  public Handler<Message<JsonObject>> insertUserEventBus() {
    return userRepository.insertUser();
  }

  @Override
  public Handler<Message<String>> getUserByIdEventBus() {
    return userRepository.getUserById();
  }

  @Override
  public Handler<Message<JsonObject>> updateUserEventBus() {
    return userRepository.updateUser();
  }

  @Override
  public Handler<Message<String>> deleteUserEventBus() {
    return userRepository.deleteUser();
  }



}
