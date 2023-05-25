package org.example.eventbus.ebservice;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.example.eventbus.ebservice.model.User;

import java.util.List;

@VertxGen
@ProxyGen
public interface UserService {

    void getUser(Handler<AsyncResult<List<User>>> handler);

    static UserService createProxy(Vertx vertx, String address){
        return new UserServiceVertxEBProxy(vertx, address);
    }
}
