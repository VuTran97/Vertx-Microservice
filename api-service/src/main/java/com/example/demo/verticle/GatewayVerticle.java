package com.example.demo.verticle;

import com.example.demo.enums.EventAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class GatewayVerticle extends AbstractVerticle {
    @Override
    public void start(){
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/user").handler(this::getUsers);
        router.post("/user").handler(this::insertOne);
        router.get("/user/:id").handler(this::getOne);
        router.put("/user").handler(this::updateOne);
        router.delete("/user/:id").handler(this::deleteOne);
        router.get("/nginx").handler(this::test);
        vertx.createHttpServer().requestHandler(router).rxListen(8081).subscribe();
    }

    private void test(RoutingContext routingContext) {
        routingContext.response().end("Hello");
    }

    private void deleteOne(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        vertx.eventBus().<String>rxSend(EventAddress.DELETE_USER.name(), id).map(Message::body)
                .subscribe(result -> onSuccessResponse(routingContext, 200, result),
                        error -> onErrorResponse(routingContext, 400, error));

    }

    private void updateOne(RoutingContext routingContext) {
        JsonObject body = routingContext.getBodyAsJson();
        vertx.eventBus().<String>rxSend(EventAddress.UPDATE_USER.name(), body).map(Message::body)
                .subscribe(result -> onSuccessResponse(routingContext, 200, result),
                        error -> onErrorResponse(routingContext, 400, error));
    }

    private void getOne(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        vertx.eventBus().<String>rxSend(EventAddress.GET_USER_BY_ID.name(), id).map(Message::body)
                .subscribe(result -> onSuccessResponse(routingContext, 200, result),
                        error -> onErrorResponse(routingContext, 400, error));
    }

    private void insertOne(RoutingContext routingContext) {
        JsonObject body = routingContext.getBodyAsJson();
        vertx.eventBus().<String>rxSend(EventAddress.INSERT_USER.name(), body).map(Message::body)
                .subscribe(result -> onSuccessResponse(routingContext, 201, result),
                        error -> onErrorResponse(routingContext, 400, error));
    }

    private void getUsers(RoutingContext routingContext) {
        vertx.eventBus().<String>rxSend(EventAddress.GET_ALL_USER.name(), "").map(Message::body)
                .subscribe(
                result -> onSuccessResponse(routingContext, 200, result),
                error -> onErrorResponse(routingContext, 400, error)
        );
        //vertx.eventBus().publish(EventAddress.GET_ALL_USER.name(), "");
    }

    private void onErrorResponse(RoutingContext rc, int status, Throwable throwable) {
        final JsonObject error = new JsonObject().put("error", throwable.getMessage());

        rc.response()
                .setStatusCode(status)
                .putHeader("Content-Type", "application/json")
                .end(error.toString());
    }

    private void onSuccessResponse(RoutingContext rc, int status, Object object) {
        rc.response()
                .setStatusCode(status)
                .putHeader("Content-Type", "application/json")
                .end(object.toString());
    }

    private void defaultResponse(RoutingContext ctx, AsyncResult<Message<String>> responseHandler) {
        if (responseHandler.failed()) {
            ctx.fail(500);
        } else {
            final Message<String> result = responseHandler.result();
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            ctx.response().end(result.body());
        }
    }

}
