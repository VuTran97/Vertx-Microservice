package com.example.demo.service;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public interface UserVersionService {

    Handler<Message<Object>> getAll();

    Handler<Message<JsonObject>> insert();
}
