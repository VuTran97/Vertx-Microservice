package com.example.demo.verticle;

import com.example.demo.helper.DeployHelper;
import com.example.demo.util.cluster.ClusterUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.example.verticle.UserVerticle;
import org.example.verticle.UserVerticleTemp;

public class ApiGatewayApp extends AbstractVerticle{

    private DeployHelper deployHelper = new DeployHelper();

    @Override
    public void start(Future<Void> future) throws Exception {
        deployHelper.deployHelper(UserVerticle.class.getName(), vertx)
                .andThen(deployHelper.deployHelper(UserVerticleTemp.class.getName(), vertx))
                .andThen(deployHelper.deployHelper(ApiRouteVerticle.class.getName(), vertx))
                .andThen(deployHelper.deployHelper(ApiGatewayVerticle.class.getName(), vertx))
                .andThen(deployHelper.deployHelper(UserVersionVerticle.class.getName(), vertx))
                .subscribe(() -> {
                    future.complete();
                }, error -> {
                    future.fail(error);
                });

    }

    public static void main(String[] args) {
        Vertx vertx1 = Vertx.vertx();
        vertx1.deployVerticle(new ApiGatewayApp());
        //ClusterUtil.clusterDeploy(new ApiGatewayVerticle());
    }

}