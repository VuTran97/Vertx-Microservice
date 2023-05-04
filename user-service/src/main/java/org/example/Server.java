package org.example;


import io.vertx.reactivex.core.Vertx;
import org.example.verticle.UserVerticle;

public class Server {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.rxDeployVerticle(new UserVerticle()).subscribe();
    }
}
