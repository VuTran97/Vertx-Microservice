package org.example.verticle;

import com.example.demo.enums.EventAddress;
import com.example.demo.repository.UserVersionRepository;
import com.example.demo.util.discovery.ServiceDiscoveryCommon;
import com.example.demo.util.kafka.KafkaConfig;
import io.reactivex.Completable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.serviceproxy.ServiceBinder;
import org.example.eventbus.UserEventBus;
import org.example.eventbus.ebservice.impl.UserServiceImpl;
import org.example.repository.UserRepository;
import org.example.service.UserService;

public class UserVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

  private KafkaConsumer<String, String> consumer;

  private KafkaConfig kafkaConfig = new KafkaConfig();

  private ServiceDiscovery discovery;

  private UserService userService;

  private org.example.eventbus.ebservice.UserService userService1;

  /**
   * define route for User APIs and create server listen on port 8000
   * create instance for UserRepository & UserService
   */
  @Override
  public void start(Future<Void> future){
    //kafka consumer config
    consumer = kafkaConfig.kafkaConsumerConfig(vertx);

    discovery = ServiceDiscovery.create(vertx);
    MongoClient client = createMongoClient(vertx);
    UserRepository userRepository = new UserRepository(client);
    UserVersionRepository userVersionRepository = new UserVersionRepository(client);
    UserEventBus userEventBus = new UserEventBus(userRepository, userVersionRepository);
    userService1 = new UserServiceImpl(userEventBus);
    createEBService().andThen(getData()).subscribe();

    userService = new org.example.service.impl.UserServiceImpl(userEventBus);
    vertx.eventBus().consumer(EventAddress.GET_ALL_USER.name(), userService.getAll(vertx));
    vertx.eventBus().consumer(EventAddress.INSERT_USER.name(), userService.insert(vertx));
    vertx.eventBus().consumer(EventAddress.GET_USER_BY_ID.name(), userService.getById(vertx));
    vertx.eventBus().consumer(EventAddress.UPDATE_USER.name(), userService.update(vertx));
    vertx.eventBus().consumer(EventAddress.DELETE_USER.name(), userService.delete(vertx));

    Router router = Router.router(vertx);
    router.get("/user").handler(routingContext -> routingContext.response().end("User service 2"));
    ServiceDiscoveryCommon serviceDiscoveryCommon = new ServiceDiscoveryCommon();
    //serviceDiscoveryCommon.publish(discovery, "user-service", "localhost", 8082, "user");
    listeningTopic(consumer);
    vertx.createHttpServer().requestHandler(router).listen(8082, httpServerAsyncResult -> {
      if(httpServerAsyncResult.succeeded()){
        logger.info("Server listening on port 8082...");
        future.complete();
      }else{
        future.fail(httpServerAsyncResult.cause());
      }
    });
  }

  private Completable createEBService(){
    return Completable.create(completableEmitter -> {
      new ServiceBinder(vertx).setAddress("user.service")
              .register(org.example.eventbus.ebservice.UserService.class, userService1);
      completableEmitter.onComplete();
    });
  }

  private Completable getData(){
    return Completable.create(completableEmitter -> {
      userService1.getUser(listAsyncResult -> {
        if (listAsyncResult.succeeded()){
          System.out.println("dgfdf: "+listAsyncResult.result());
          completableEmitter.onComplete();
        }else{
          completableEmitter.onError(listAsyncResult.cause());
        }
      });
    });

  }

  private void listeningTopic(KafkaConsumer<String, String> consumer){
    consumer.subscribe("user-topic1");
    consumer.handler(record -> {
      logger.info("Processing: key={0}, value={1}, partition={2}, offset={3}", record.key(), record.value(), record.partition(), record.offset());
    });
  }

  /**
   * create connect with mongo
   */
  private MongoClient createMongoClient(Vertx vertx){
    JsonObject mongoconfig = new JsonObject().put("connection_string", "mongodb://localhost:27017").put("db_name", "vertx");
    return MongoClient.createShared(vertx, mongoconfig);
  }
}
