package com.example.demo.verticle;

import com.example.demo.util.loadbalance.WeightRoundRobin;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApiGatewayVerticle extends AbstractVerticle {

    private ServiceDiscovery discovery;

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayVerticle.class);

    @Override
    public void start() {
        discovery = ServiceDiscovery.create(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route("/api/*").handler(this::dispatchRequest);
        Completable.create(completableEmitter -> {
            vertx.createHttpServer().requestHandler(router).listen(8787, httpServerAsyncResult -> {
                if(httpServerAsyncResult.succeeded()){
                    logger.info("Server listening on port 8787...");
                    completableEmitter.onComplete();
                }else{
                    completableEmitter.onError(httpServerAsyncResult.cause());
                }
            });
        }).subscribe();
    }

    private void dispatchRequest(RoutingContext routingContext) {
        int initialUrl = 4; // /api
        getAllEndpoint().subscribe(records -> {
            String path = routingContext.request().uri();
            if(path.length() <= initialUrl){
                logger.info("path not found");
                return;
            }
            String prefix = path.substring(initialUrl);
            String api_name = (path.substring(5)
                    .split("/"))[0];

            List<Record> results = records.stream().filter(rc -> rc.getMetadata().getString("api.name") != null)
                    .filter(rc -> rc.getMetadata().getString("api.name").equals(api_name))
                    .collect(Collectors.toList());

            List<String> ipList = results.stream().map(item -> item.getLocation().getString("host").concat(":").concat(String.valueOf(item.getLocation().getInteger("port"))))
                    .sorted().collect(Collectors.toList());

            Optional<Record> record = Optional.empty();
            if(ipList.size() > 0){
                //round robin load balancer
                //RoundRobin roundRobin = new RoundRobin();
                //String server = roundRobin.roundRobin(ipList);

                //weight round robin
                WeightRoundRobin weightRoundRobin = new WeightRoundRobin();
                String server = weightRoundRobin.weightRoundRobin(ipList);
                record = results.stream().filter(item -> item.getLocation().getString("host").concat(":").concat(String.valueOf(item.getLocation().getInteger("port"))).equalsIgnoreCase(server)).findFirst();
            }

            if(record.isPresent()){
                ServiceReference serviceReference = discovery.getReference(record.get());
                WebClient webClient = serviceReference.getAs(WebClient.class);
                doDispatchWebClient(routingContext, prefix, webClient);
            }else{
                routingContext.response().setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("message", "not_found").encodePrettily());
            }
        }, error -> {
            logger.error(error);
        });
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

    private Single<List<Record>> getAllEndpoint(){
        return Single.create(singleEmitter -> {
            discovery.getRecords(r -> true, listAsyncResult -> {
                if(listAsyncResult.succeeded()){
                    singleEmitter.onSuccess(listAsyncResult.result());
                }else{
                    singleEmitter.onError(listAsyncResult.cause());
                }
            });
        });
    }
}
