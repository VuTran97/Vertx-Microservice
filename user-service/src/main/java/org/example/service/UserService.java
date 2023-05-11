package org.example.service;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public interface UserService {

    Handler<Message<Object>> getAll();

    Handler<Message<JsonObject>> insert();

    Handler<Message<String>> getById();

    Handler<Message<JsonObject>> update();

    Handler<Message<String>> delete();
}
