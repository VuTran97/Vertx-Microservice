package org.example.service;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import org.example.entity.User;

import java.util.List;

public interface UserService {
    Maybe<User> insert(User user);

    Single<List<User>> getAll();

    Maybe<User> getById(String id);

    Completable update(String id, User user);

    Completable delete(String id);

    Handler<Message<Object>> getAllUserEventBus();

    Handler<Message<JsonObject>> insertUserEventBus();

    Handler<Message<Object>> getUserByIdEventBus();

    Handler<Message<JsonObject>> updateUserEventBus();

    Handler<Message<String>> deleteUserEventBus();
}
