package org.example.verticle;

import com.example.demo.enums.EventAddress;
import com.example.demo.util.discovery.ServiceDiscoveryCommon;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.ServiceDiscovery;
import org.example.eventbus.UserEventBus;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.service.impl.UserServiceImpl;

public class UserVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

  private ServiceDiscovery discovery;

  private UserService userService;

  /**
   * define route for User APIs and create server listen on port 8000
   * create instance for UserRepository & UserService
   */
  @Override
  public void start(){
    discovery = ServiceDiscovery.create(vertx);
    MongoClient client = createMongoClient(vertx);
    UserRepository userRepository = new UserRepository(client);
    UserEventBus userEventBus = new UserEventBus(userRepository);
    userService = new UserServiceImpl(userEventBus);
    vertx.eventBus().consumer(EventAddress.GET_ALL_USER.name(), userService.getAll());
    vertx.eventBus().consumer(EventAddress.INSERT_USER.name(), userService.insert());
    vertx.eventBus().consumer(EventAddress.GET_USER_BY_ID.name(), userService.getById());
    vertx.eventBus().consumer(EventAddress.UPDATE_USER.name(), userService.update());
    vertx.eventBus().consumer(EventAddress.DELETE_USER.name(), userService.delete());

    Router router = Router.router(vertx);
    router.get("/user").handler(routingContext -> routingContext.response().end("User service 2"));
    ServiceDiscoveryCommon serviceDiscoveryCommon = new ServiceDiscoveryCommon();
    serviceDiscoveryCommon.publish(discovery, "user-service", "localhost", 8082, "user");
    Completable.create(completableEmitter -> {
      vertx.createHttpServer().requestHandler(router).listen(8082, httpServerAsyncResult -> {
        if(httpServerAsyncResult.succeeded()){
          logger.info("Server listening on port 8082...");
          completableEmitter.onComplete();
        }else{
          completableEmitter.onError(httpServerAsyncResult.cause());
        }
      });
    }).subscribe();
  }

  /**
   * create connect with mongo
   */
  private MongoClient createMongoClient(Vertx vertx){
    JsonObject mongoconfig = new JsonObject().put("connection_string", "mongodb://localhost:27017").put("db_name", "vertx");
    return MongoClient.createShared(vertx, mongoconfig);
  }
}
