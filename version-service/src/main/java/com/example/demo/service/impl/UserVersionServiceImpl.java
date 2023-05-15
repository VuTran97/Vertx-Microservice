package com.example.demo.service.impl;

import com.example.demo.eventbus.UserVersionEventBus;
import com.example.demo.service.UserVersionService;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class UserVersionServiceImpl implements UserVersionService {

    private final UserVersionEventBus userVersionEventBus;

    public UserVersionServiceImpl(UserVersionEventBus userVersionEventBus) {
        this.userVersionEventBus = userVersionEventBus;
    }


    @Override
    public Handler<Message<String>> getAll() {
        return userVersionEventBus.getAll();
    }

    @Override
    public Handler<Message<JsonObject>> insert() {
        return userVersionEventBus.insert();
    }

    @Override
    public Handler<Message<String>> getByUserId() {
        return userVersionEventBus.getByUserId();
    }

    @Override
    public Handler<Message<String>> deleteByUserId() {
        return userVersionEventBus.delete();
    }
}
