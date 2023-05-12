package com.example.demo.eventbus;

import com.example.demo.repository.UserVersionRepository;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class UserVersionEventBus {

    private final UserVersionRepository userVersionRepository;

    public UserVersionEventBus(UserVersionRepository userVersionRepository) {
        this.userVersionRepository = userVersionRepository;
    }

    public Handler<Message<Object>> getAll(){
        return handler -> {
            userVersionRepository.getAll().subscribe(item -> {
                handler.reply(new JsonArray(item).encodePrettily());
            }, error -> {
                handler.fail(400, error.getMessage());
            });
        };
    }

    public Handler<Message<JsonObject>> insert(){
        return handler -> {
            JsonObject body = handler.body();
            userVersionRepository.insert(body).subscribe(item -> {
                handler.reply(item.encodePrettily());
            }, error -> {
                handler.fail(400, error.getMessage());
            });
        };
    }
}
