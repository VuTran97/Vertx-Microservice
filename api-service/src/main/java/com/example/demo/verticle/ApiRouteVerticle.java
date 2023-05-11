package com.example.demo.verticle;

import com.example.demo.enums.EventAddress;
import com.example.demo.util.discovery.ServiceDiscoveryCommon;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.ServiceDiscovery;

public class ApiRouteVerticle extends AbstractVerticle {

    private ServiceDiscovery discovery;

    private static final Logger logger = LoggerFactory.getLogger(ApiRouteVerticle.class);
    @Override
    public void start(){
        discovery = ServiceDiscovery.create(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/user").handler(this::getAll);
        router.post("/user").handler(this::insertOne);
        router.get("/user/:id").handler(this::getOne);
        router.put("/user").handler(this::updateOne);
        router.delete("/user/:id").handler(this::deleteOne);
        ServiceDiscoveryCommon serviceDiscoveryCommon = new ServiceDiscoveryCommon();
        serviceDiscoveryCommon.publish(discovery, "user-service", "localhost", 8081, "user");
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

    private void getAll(RoutingContext routingContext) {
        vertx.eventBus().send(EventAddress.GET_ALL_USER.name(), "", (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler));
    }

    private void defaultResponse(RoutingContext ctx, AsyncResult<Message<String>> responseHandler) {
        if (responseHandler.failed()) {
            ReplyException cause = (ReplyException) responseHandler.cause();
            ctx.response().setStatusCode(cause.failureCode()).end(responseHandler.cause().getMessage());
        } else {
            final Message<String> result = responseHandler.result();
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(result.body());
        }
    }

}
