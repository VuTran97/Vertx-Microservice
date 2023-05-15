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

    public Handler<Message<String>> getAll(){
        return handler -> {
            userVersionRepository.getAll().subscribe(item -> {
                handler.reply(new JsonArray(item));
            }, error -> {
                handler.fail(400, error.getMessage());
            });
        };
    }

    public Handler<Message<String>> getByUserId(){
        return handler -> {
            String userId = handler.body();
            userVersionRepository.getByUserId(userId).subscribe(item -> {
                handler.reply(new JsonArray(item));
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

    public Handler<Message<String>> delete(){
        return handler -> {
            String userId = handler.body();
            userVersionRepository.delete(userId).subscribe(() -> {
                handler.reply("delete success");
            }, error -> {
                handler.fail(400, error.getMessage());
            });
        };
    }
}
