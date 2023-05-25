package com.example.demo.verticle;

import com.example.demo.enums.EventAddress;
import com.example.demo.exception.VerticleException;
import com.example.demo.util.discovery.ServiceDiscoveryCommon;
import com.example.demo.util.kafka.KafkaConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.api.validation.ValidationException;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import io.vertx.servicediscovery.ServiceDiscovery;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiRouteVerticle extends AbstractVerticle {


    private KafkaProducer<String, JsonObject> producer;
    private final KafkaConfig kafkaConfig = new KafkaConfig();

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = LoggerFactory.getLogger(ApiRouteVerticle.class);

    HttpServer server;

    @Override
    public void start(Future<Void> future) {
        //kafka config producer
        producer = kafkaConfig.kafkaProducerConfig(vertx);

        producer.partitionsFor("user-topic1", done -> done.result().forEach(p -> logger.info("Partition: id={0}, topic={1}", p.getPartition(), p.getTopic())));
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        //open api
        createRouterFactory(vertx).subscribe(openAPI3RouterFactory -> {
            openAPI3RouterFactory.setOptions(new RouterFactoryOptions().setMountNotImplementedHandler(true).setMountValidationFailureHandler(true));
            openAPI3RouterFactory.addHandlerByOperationId("listUsers", this::getAll);
            openAPI3RouterFactory.addHandlerByOperationId("createUser", this::insertOne);
            openAPI3RouterFactory.addHandlerByOperationId("getUserById", this::getOne);
            openAPI3RouterFactory.addHandlerByOperationId("updateUser", this::updateOne);
            openAPI3RouterFactory.addHandlerByOperationId("deleteUserById", this::deleteOne);
            Router router = openAPI3RouterFactory.getRouter();

            router.errorHandler(400, routingContext -> {
                Throwable throwable = routingContext.failure();
                if (routingContext.failure() instanceof VerticleException) {
                    VerticleException exception = (VerticleException) routingContext.failure();

                    final JsonObject error = new JsonObject()
                            .put("timestamp", dtf.format(LocalDateTime.now()))
                            .put("status", exception.getStatusCode())
                            .put("error", HttpResponseStatus.valueOf(exception.getStatusCode()).reasonPhrase())
                            .put("path", routingContext.normalisedPath());

                    if (exception.getMessage() != null) {
                        error.put("message", exception.getMessage());
                    }
                    routingContext.response().setStatusCode(exception.getStatusCode());
                    routingContext.response().end(error.encode());
                } else if (throwable instanceof ValidationException) {
                    final JsonObject error = new JsonObject()
                            .put("timestamp", dtf.format(LocalDateTime.now()))
                            .put("path", routingContext.normalisedPath())
                            .put("message", throwable.getMessage());
                    routingContext.response().setStatusCode(400);
                    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(error.encode());
                } else {
                    routingContext.response().setStatusCode(400).end(new JsonObject().put("message", "bad_request").encodePrettily());
                }
            });
            vertx.createHttpServer().requestHandler(router).listen(8081, httpServerAsyncResult -> {
                if (httpServerAsyncResult.succeeded()) {
                    logger.info("Server listening on port 8081...");
                    ServiceDiscoveryCommon serviceDiscoveryCommon = new ServiceDiscoveryCommon();
                    serviceDiscoveryCommon.publish(discovery, "user-service", "localhost", 8081, "user");
                    future.complete();
                } else {
                    future.fail(httpServerAsyncResult.cause());
                }
            });


        });
    }

    private Single<OpenAPI3RouterFactory> createRouterFactory(Vertx vertx) {
        return Single.create(singleEmitter -> {
            OpenAPI3RouterFactory.create(vertx, "user.yaml", ar -> {
                if (ar.failed()) {
                    logger.error("Error while read file {0}: ", ar.cause());
                    singleEmitter.onError(ar.cause());
                } else {
                    singleEmitter.onSuccess(ar.result());
                }
            });
        });
    }

    private void sendKafkaMsg(JsonObject body) {
        KafkaProducerRecord<String, JsonObject> record = KafkaProducerRecord.create("user-topic1", null, body);
        producer.write(record, done -> {
            if (done.succeeded()) {
                RecordMetadata recordMetadata = done.result();
                logger.info("Record sent: msg={0}, destination={1}, partition={2}, offset={3}", record.value(), recordMetadata.getTopic(), recordMetadata.getPartition(), recordMetadata.getOffset());
            } else {
                Throwable t = done.cause();
                logger.error("Error sent to topic: {0}", t.getMessage());
            }
        });
    }

    private void deleteOne(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        vertx.eventBus().send(EventAddress.DELETE_USER.name(), id, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler, new JsonObject().put("id", id).put("action", "delete")));
    }

    private void updateOne(RoutingContext routingContext) {
        JsonObject body = routingContext.getBodyAsJson();
        vertx.eventBus().send(EventAddress.UPDATE_USER.name(), body, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler, body));
    }

    private void getOne(RoutingContext routingContext) {
        String id = routingContext.pathParam("id");
        vertx.eventBus().send(EventAddress.GET_USER_BY_ID.name(), id, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler, new JsonObject().put("id", id).put("action", "getById")));
    }

    private void insertOne(RoutingContext routingContext) {
        JsonObject body = routingContext.getBodyAsJson();
        vertx.eventBus().send(EventAddress.INSERT_USER.name(), body, (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler, body));
    }

    private void getAll(RoutingContext routingContext) {
        vertx.eventBus().send(EventAddress.GET_ALL_USER.name(), "", (Handler<AsyncResult<Message<String>>>) replyHandler -> defaultResponse(routingContext, replyHandler, new JsonObject()));
    }

    private void defaultResponse(RoutingContext ctx, AsyncResult<Message<String>> responseHandler, JsonObject kafkaData) {
        ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        if (responseHandler.failed()) {
            ReplyException cause = (ReplyException) responseHandler.cause();
            ctx.fail(400, new VerticleException(cause, cause.failureCode()));
            kafkaData.put("error", cause.getMessage());
        } else {
            final Message<String> result = responseHandler.result();
            ctx.response().end(result.body());
        }
        sendKafkaMsg(kafkaData);
    }

}
