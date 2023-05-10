package com.example.demo;

import com.example.demo.verticle.ApiGatewayVerticle;
import com.example.demo.verticle.ApiRouteVerticle;
import io.vertx.core.Vertx;
import org.example.verticle.UserVerticle;
import org.example.verticle.UserVerticleTemp;

public class Server {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new UserVerticle());
        vertx.deployVerticle(new ApiRouteVerticle());
        vertx.deployVerticle(new ApiGatewayVerticle());
    }
}