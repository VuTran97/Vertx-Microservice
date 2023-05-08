package org.example.verticle;

import com.example.demo.enums.EventAddress;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
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
    userService = new UserServiceImpl(userRepository);
    vertx.eventBus().consumer(EventAddress.GET_ALL_USER.name(), userService.getAllUserEventBus());
    vertx.eventBus().consumer(EventAddress.INSERT_USER.name(), userService.insertUserEventBus());
    vertx.eventBus().consumer(EventAddress.GET_USER_BY_ID.name(), userService.getUserByIdEventBus());
    vertx.eventBus().consumer(EventAddress.UPDATE_USER.name(), userService.updateUserEventBus());
    vertx.eventBus().consumer(EventAddress.DELETE_USER.name(), userService.deleteUserEventBus());
    Router router = Router.router(vertx);
    router.get("/user").handler(routingContext -> {
      routingContext.response().end("User service 2");
    });
    publish();
    vertx.createHttpServer().requestHandler(router).listen(8082);
  }

  /**
   * create connect with mongo
   * @param vertx
   * @return
   */
  private MongoClient createMongoClient(Vertx vertx){
    JsonObject mongoconfig = new JsonObject().put("connection_string", "mongodb://localhost:27017").put("db_name", "vertx");
    return MongoClient.createShared(vertx, mongoconfig);
  }

  private void publish(){
    Record record = HttpEndpoint.createRecord("user-service", "localhost", 8082, "/",
            new JsonObject().put("api.name", "user"));
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
