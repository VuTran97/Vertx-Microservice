package org.example.service.impl;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.eventbus.Message;
import org.example.converter.UserConverter;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;

import java.util.List;

public class UserServiceImpl implements UserService {


  private final UserRepository userRepository;

  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  private UserConverter userConverter = new UserConverter();

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


  @Override
  public Maybe<User> insert(User user) {
    return userRepository.insert(user);

  }

    @Override
    public Single<List<User>> getAll() {
        return userRepository.getAll();
    }

  @Override
  public Maybe<User> getById(String id) {
    return userRepository.getById(id);
  }

  @Override
  public Completable update(String id, User user) {
    return userRepository.update(id, user);
  }

  @Override
  public Completable delete(String id) {
    return userRepository.delete(id);
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
  public Handler<Message<Object>> getUserByIdEventBus() {
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
