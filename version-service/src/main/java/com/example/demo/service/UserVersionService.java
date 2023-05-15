package com.example.demo.service;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public interface UserVersionService {

    Handler<Message<String>> getAll();

    Handler<Message<JsonObject>> insert();

    Handler<Message<String>> getByUserId();

    Handler<Message<String>> deleteByUserId();
}
