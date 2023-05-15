package com.example.demo.verticle;

import com.example.demo.enums.EventAddress;
import com.example.demo.exception.VerticleException;
import com.example.demo.util.discovery.ServiceDiscoveryCommon;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiRouteVerticle extends AbstractVerticle {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = LoggerFactory.getLogger(ApiRouteVerticle.class);
    @Override
    public void start(){
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        //user route
        router.get("/user").handler(this::getAll);
        router.post("/user").handler(this::insertOne);
        router.get("/user/:id").handler(this::getOne);
        router.put("/user").handler(this::updateOne);
        router.delete("/user/:id").handler(this::deleteOne);
        router.route().failureHandler(routingContext -> {
            if(routingContext.failure() instanceof VerticleException){
                VerticleException exception = (VerticleException) routingContext.failure();

                final JsonObject error = new JsonObject()
                        .put("timestamp", dtf.format(LocalDateTime.now()))
                        .put("status", exception.getStatusCode())
                        .put("error", HttpResponseStatus.valueOf(exception.getStatusCode()).reasonPhrase())
                        .put("path", routingContext.normalisedPath());

                if(exception.getMessage() != null) {
                    error.put("message", exception.getMessage());
                }
                routingContext.response().setStatusCode(exception.getStatusCode());
                routingContext.response().end(error.encode());
            }
        });
        ServiceDiscoveryCommon serviceDiscoveryCommon = new ServiceDiscoveryCommon();
        serviceDiscoveryCommon.publish(discovery, "user-service", "localhost", 8081, "user");
        Completable.create(completableEmitter -> {
            vertx.createHttpServer().requestHandler(router).listen(8081, httpServerAsyncResult -> {
                if(httpServerAsyncResult.succeeded()){
                    logger.info("Server listening on port 8081...");
                    completableEmitter.onComplete();
                }else{
                    completableEmitter.onError(httpServerAsyncResult.cause());
                }
            });
        }).subscribe();
    }

    private void deleteVersion(RoutingContext routingContext) {
        String userId = routingContext.pathParam("id");
        vertx.eventBus().send(EventAddress.DELETE_VERSION_BY_USER_ID.name(), userId, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler));

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
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        if (responseHandler.failed()) {
            ReplyException cause = (ReplyException) responseHandler.cause();
            ctx.fail(new VerticleException(cause, cause.failureCode()));
        } else {
            final Message<String> result = responseHandler.result();
            ctx.response().end(result.body());
        }
    }

}
