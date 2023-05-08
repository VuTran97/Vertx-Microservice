package org.example.service;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public interface UserService {

    Handler<Message<Object>> getAllUserEventBus();

    Handler<Message<JsonObject>> insertUserEventBus();

    Handler<Message<String>> getUserByIdEventBus();

    Handler<Message<JsonObject>> updateUserEventBus();

    Handler<Message<String>> deleteUserEventBus();
}
