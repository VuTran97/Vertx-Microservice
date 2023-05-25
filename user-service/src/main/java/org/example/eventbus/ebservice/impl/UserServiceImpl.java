package org.example.eventbus.ebservice.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.example.eventbus.UserEventBus;
import org.example.eventbus.ebservice.model.User;
import org.example.eventbus.ebservice.UserService;
import org.example.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {


    private UserEventBus userEventBus;

    public UserServiceImpl(UserEventBus eventBus){
        this.userEventBus = eventBus;
    }
    @Override
    public void getUser(Handler<AsyncResult<List<User>>> handler) {
            userEventBus.getAllEB().subscribe(jsonObjects -> {
                List<JsonObject> a = new ArrayList<>();
                List<User> versions = jsonObjects.stream()
                .map(jsonObject -> new User(jsonObject)).collect(Collectors.toList());
                handler.handle(Future.succeededFuture(versions));
            });
    }
}
