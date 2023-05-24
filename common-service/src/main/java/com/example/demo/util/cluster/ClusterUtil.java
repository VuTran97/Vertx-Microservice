package com.example.demo.util.cluster;

import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.Collections;

public class ClusterUtil {

    private ClusterUtil() {
    }

    public static Config hzcConfig() {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig()
                .setEnabled(true).setMembers(Collections.singletonList("127.0.0.1"));
        return config;
    }

    public static ClusterManager hzcClusterManager() {
        return new HazelcastClusterManager(hzcConfig());
    }

    public static VertxOptions commonVertxOptions() {
        return new VertxOptions().setClusterManager(hzcClusterManager());
    }

    public static void clusterDeploy(AbstractVerticle verticle) {
        Vertx.clusteredVertx(commonVertxOptions(), ar -> {
            if (ar.failed()) {
                throw new IllegalStateException("Can't deploy ");
            } else {
                Vertx vertx = ar.result();
                vertx.deployVerticle(verticle);
            }
        });
    }
}
