package org.example.verticle;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.example.service.impl.UserServiceImpl;

import java.util.NoSuchElementException;

public class UserVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

  private UserService userService;
  /**
   * define route for User APIs and create server listen on port 8000
   * create instance for UserRepository & UserService
   */
  @Override
  public void start() throws Exception {
    MongoClient client = createMongoClient(vertx);
    UserRepository userRepository = new UserRepository(client);
    userService = new UserServiceImpl(userRepository);
    Router router = Router.router(vertx);
    //vertx.eventBus().consumer("/user", userRepository.getAllUser());
    router.route().handler(BodyHandler.create());
    router.post("/user").handler(this::insertOne);
    router.get("/user").handler(this::getAllUser);
    //router.get("/user/:id").handler(this::getUserById);
    router.put("/user/:id").handler(this::updateOne);
    router.delete("/user/:id").handler(this::deleteOne);
    //router.get("/user/eventbus").handler(this::getAllUserByEventbus);
    vertx.createHttpServer().requestHandler(router).listen(8000);
  }

//  private void getAllUserByEventbus(RoutingContext routingContext) {
//    vertx.eventBus().<String>rxSend("/user", "").map(Message::body).subscribe(m -> {
//      routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(m);
//    });
//  }


  /**
   * handle for delete user
   * @param routingContext
   */
  private void deleteOne(RoutingContext routingContext) {
    String id = routingContext.pathParam("id");
    userService.delete(id).subscribe(
            () -> onSuccessResponse(routingContext, 204, null),
            error -> onErrorResponse(routingContext, 400, error)
    );
  }

  /**
   * handle for update user
   * @param routingContext
   */
  private void updateOne(RoutingContext routingContext) {
    String id = routingContext.pathParam("id");
    User user = mapRequestBodyToUser(routingContext);
    userService.update(id, user).subscribe(
            () -> onSuccessResponse(routingContext, 204, null),
            error -> onErrorResponse(routingContext, 400, error)
    );
  }

  /**
   * handle get user by id
   * @param routingContext
   */
  private void getUserById(RoutingContext routingContext) {
    String id = routingContext.pathParam("id");
    userService.getById(id).subscribe(
            result -> onSuccessResponse(routingContext,200,result),
            error -> onErrorResponse(routingContext,400,error),
            () ->onErrorResponse(routingContext, 400, new NoSuchElementException("No User with id: "+id))
    );
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

  /**
   * handle get all user
   * @param routingContext
   */
  private void getAllUser(RoutingContext routingContext) {
      userService.getAll()
              .subscribe(result -> onSuccessResponse(routingContext, 200, result),
                      error -> onErrorResponse(routingContext, 400, error));
  }

  /**
   * handle for response success
   * @param rc
   * @param status
   * @param object
   */
  private void onSuccessResponse(RoutingContext rc, int status, Object object) {
    rc.response()
            .setStatusCode(status)
            .putHeader("Content-Type", "application/json")
            .end(Json.encodePrettily(object));
  }

  /**
   * handle for response error
   * @param rc
   * @param status
   * @param throwable
   */
  private void onErrorResponse(RoutingContext rc, int status, Throwable throwable) {
    final JsonObject error = new JsonObject().put("error", throwable.getMessage());

    rc.response()
            .setStatusCode(status)
            .putHeader("Content-Type", "application/json")
            .end(Json.encodePrettily(error));
  }

  /**
   * handle create User
   * @param routingContext
   */
  private void insertOne(RoutingContext routingContext) {
    final User user = mapRequestBodyToUser(routingContext);

    userService.insert(user).subscribe(
            result -> onSuccessResponse(routingContext, 201, user),
            error -> onErrorResponse(routingContext, 400, error)
    );

  }

  /**
   * Mapping between user class and request body JSON object
   */
  private User mapRequestBodyToUser(RoutingContext rc) {
    User user = new User();

    try {
      user = rc.getBodyAsJson().mapTo(User.class);
    } catch (IllegalArgumentException ex) {
      onErrorResponse(rc, 400, ex);
    }
    return user;
  }

}
