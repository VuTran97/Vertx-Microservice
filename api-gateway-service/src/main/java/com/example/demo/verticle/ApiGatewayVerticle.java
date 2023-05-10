package com.example.demo.verticle;

import com.example.demo.util.loadbalance.RoundRobin;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import org.example.verticle.UserVerticle;
import org.example.verticle.UserVerticleTemp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ApiGatewayVerticle extends AbstractVerticle {

    private ServiceDiscovery discovery;

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayVerticle.class);

    @Override
    public void start() {
//        vertx.deployVerticle(new UserVerticle());
//        vertx.deployVerticle(new UserVerticleTemp());
//        vertx.deployVerticle(new ApiRouteVerticle());
        discovery = ServiceDiscovery.create(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route("/api/*").handler(this::dispatchRequest);
        vertx.createHttpServer().requestHandler(router).listen(8787);
    }

    private void dispatchRequest(RoutingContext routingContext) {
        int initialUrl = 4; // /api
        List<Record> records = getAllEndpoint();
        String path = routingContext.request().uri();
        if(path.length() <= initialUrl){
            logger.info("path not found");
            return;
        }
        String prefix = path.substring(initialUrl);
        String api_name = (path.substring(5)
                .split("/"))[0];
        Optional<Record> record = records.stream().filter(rc -> rc.getMetadata().getString("api.name") != null)
                .filter(rc -> rc.getMetadata().getString("api.name").equals(api_name))
                .findAny();

        if(record.isPresent()){
            ServiceReference serviceReference = discovery.getReference(record.get());
            WebClient webClient = serviceReference.getAs(WebClient.class);
            doDispatchHttpClient(routingContext, prefix, discovery.getReference(record.get()).get());
            //doDispatchWebClient(routingContext, prefix, webClient);
        }else{
            routingContext.response().setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("message", "not_found").encodePrettily());
        }
    }


    private void doDispatchHttpClient(RoutingContext routingContext, String newPath, HttpClient client) {
        HttpClientRequest req =
                client.request(routingContext.request().method(), newPath, res -> res.bodyHandler(body -> {
                    routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
                                    .setStatusCode(res.statusCode()).end(body);
                    ServiceDiscovery.releaseServiceObject(discovery,vertx);
                }));
        if(routingContext.getBody() == null){
            req.end();
        }
        req.end(routingContext.getBody());
    }

    private void doDispatchWebClient(RoutingContext routingContext, String newPath, WebClient client) {
        client.request(routingContext.request().method(), newPath)
                .sendBuffer(routingContext.getBody(), res -> {
                    if(res.succeeded()){
                        routingContext.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .setStatusCode(res.result().statusCode())
                                .end(res.result().body());
                    }else{
                        logger.error(res.cause().getMessage());
                    }
                });
    }

    private List<Record> getAllEndpoint(){
        AtomicReference<List<Record>> result = new AtomicReference<>();
        discovery.getRecords(r -> true, listAsyncResult -> {
            if(listAsyncResult.succeeded()){
                result.set(listAsyncResult.result());
            }else {
                logger.error("An error occur when get service records: {0}", listAsyncResult.cause());
            }
        });
        return result.get();
    }
}
