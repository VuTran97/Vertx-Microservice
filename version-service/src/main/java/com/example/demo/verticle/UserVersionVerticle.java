package com.example.demo.verticle;

import com.example.demo.enums.EventAddress;
import com.example.demo.eventbus.UserVersionEventBus;
import com.example.demo.repository.UserVersionRepository;
import com.example.demo.service.UserVersionService;
import com.example.demo.service.impl.UserVersionServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class UserVersionVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        MongoClient mongoClient = createMongoClient(vertx);
        UserVersionRepository userVersionRepository = new UserVersionRepository(mongoClient);
        UserVersionEventBus userVersionEventBus = new UserVersionEventBus(userVersionRepository);
        UserVersionService userVersionService = new UserVersionServiceImpl(userVersionEventBus);
        vertx.eventBus().consumer(EventAddress.INSERT_USER_VERSION.name(), userVersionService.insert());
        vertx.eventBus().consumer(EventAddress.GET_ALL_USER_VERSION.name(), userVersionService.getAll());
        vertx.eventBus().consumer(EventAddress.GET_USER_VERSION_BY_USER_ID.name(), userVersionService.getByUserId());
        vertx.eventBus().consumer(EventAddress.DELETE_VERSION_BY_USER_ID.name(), userVersionService.deleteByUserId());
    }

    private MongoClient createMongoClient(Vertx vertx){
        JsonObject mongoconfig = new JsonObject().put("connection_string", "mongodb://localhost:27017").put("db_name", "vertx");
        return MongoClient.createShared(vertx, mongoconfig);
    }
}
