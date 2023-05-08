package com.example.demo.verticle;

import com.example.demo.base.BaseMicroserviceVerticle;
import com.example.demo.discovery.ServiceDiscoveryPublish;
import com.example.demo.enums.EventAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

public class GatewayVerticle extends ServiceDiscoveryPublish {

    private static final Logger logger = LoggerFactory.getLogger(BaseMicroserviceVerticle.class);
    @Override
    public void start(){
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/user").handler(this::getUsers);
        router.post("/user").handler(this::insertOne);
        router.get("/user/:id").handler(this::getOne);
        router.put("/user").handler(this::updateOne);
        router.delete("/user/:id").handler(this::deleteOne);
        BaseMicroserviceVerticle baseMicroserviceVerticle = new BaseMicroserviceVerticle();
        baseMicroserviceVerticle.publish("user-service", "localhost", 8081, "user");
        vertx.createHttpServer().requestHandler(router).listen(8081);
    }


    private void deleteOne(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        vertx.eventBus().send(EventAddress.DELETE_USER.name(), id, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler));
    }

    private void updateOne(RoutingContext routingContext) {
        JsonObject body = routingContext.getBodyAsJson();
        vertx.eventBus().send(EventAddress.UPDATE_USER.name(), body, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler));
    }

    private void getOne(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        vertx.eventBus().send(EventAddress.GET_USER_BY_ID.name(), id, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler));
    }

    private void insertOne(RoutingContext routingContext) {
        JsonObject body = routingContext.getBodyAsJson();
        vertx.eventBus().send(EventAddress.INSERT_USER.name(), body, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler));
    }

    private void getUsers(RoutingContext routingContext) {
        vertx.eventBus().send(EventAddress.GET_ALL_USER.name(), "", (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler));
    }



    private void defaultResponse(RoutingContext ctx, AsyncResult<Message<String>> responseHandler) {
        if (responseHandler.failed()) {
            ctx.response().setStatusCode(500).end(responseHandler.cause().getMessage());
        } else {
            final Message<String> result = responseHandler.result();
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(result.body());
        }
    }

}
