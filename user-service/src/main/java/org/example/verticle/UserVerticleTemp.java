package org.example.verticle;

import com.example.demo.util.discovery.ServiceDiscoveryCommon;
import io.reactivex.Completable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.ServiceDiscovery;

public class UserVerticleTemp extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

    private ServiceDiscovery discovery;

    @Override
    public void start() throws Exception {
        discovery = ServiceDiscovery.create(vertx);
        Router router = Router.router(vertx);
        router.get("/user").handler(routingContext -> {
            routingContext.response().end("User service 3");
        });
        ServiceDiscoveryCommon serviceDiscovery = new ServiceDiscoveryCommon();
        serviceDiscovery.publish(discovery, "user-service", "localhost", 8083, "user");
        Completable.create(completableEmitter -> {
            vertx.createHttpServer().requestHandler(router).listen(8083, httpServerAsyncResult -> {
                if(httpServerAsyncResult.succeeded()){
                    logger.info("Server listening on port 8083...");
                    completableEmitter.onComplete();
                }else{
                    completableEmitter.onError(httpServerAsyncResult.cause());
                }
            });
        }).subscribe();
    }

}
