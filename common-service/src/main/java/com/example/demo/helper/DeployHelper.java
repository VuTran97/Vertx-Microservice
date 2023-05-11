package com.example.demo.helper;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class DeployHelper {

    private static final Logger logger = LoggerFactory.getLogger(DeployHelper.class);

    public Future<Void> deployHelper(String name, Vertx vertx){
        final Future<Void> future = Future.future();
        vertx.deployVerticle(name, res -> {
            if(res.failed()){
                logger.error("Failed to deploy verticle " + name);
                future.fail(res.cause());
            } else {
                logger.info("Deployed verticle " + name);
                future.complete();
            }
        });
        return future;
    }

}
