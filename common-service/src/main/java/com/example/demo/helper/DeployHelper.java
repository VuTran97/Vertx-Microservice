package com.example.demo.helper;

import io.reactivex.Completable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


public class DeployHelper {

    private static final Logger logger = LoggerFactory.getLogger(DeployHelper.class);

    public Completable deployHelper(String name, Vertx vertx){
        return Completable.create(completableEmitter -> {
            vertx.deployVerticle(name, res -> {
                if(res.failed()){
                    logger.error("Failed to deploy verticle " + name);
                    completableEmitter.onError(res.cause());
                } else {
                    logger.info("Deployed verticle " + name);
                    completableEmitter.onComplete();
                }
            });
        });
    }

}
