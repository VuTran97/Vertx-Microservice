package com.example.demo.util.discovery;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

public class ServiceDiscoveryCommon {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryCommon.class);

    public void publish(ServiceDiscovery discovery, String serviceName, String host, int port, String apiName){
        Record record = HttpEndpoint.createRecord(serviceName, host, port, "/",
                new JsonObject().put("api.name", apiName));
        discovery.publish(record, rc -> {
            if (rc.succeeded()) {
                logger.info("Service <" + rc.result().getName() + "> published");
                Record recordPublished = rc.result();
                logger.info("Record is: {0}", recordPublished.getMetadata());
            } else {
                logger.error(rc.cause().getMessage());
            }
        });
    }
}
