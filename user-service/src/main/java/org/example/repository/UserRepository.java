package org.example.repository;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.example.entity.User;
import org.example.verticle.UserVerticle;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

    private static final String COLLECTION_NAME = "users";

    private final MongoClient mongoClient;

    public UserRepository(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public Single<List<User>> getAll() {
        final JsonObject query = new JsonObject();
        return mongoClient.rxFind(COLLECTION_NAME, query)
                .flatMap(result -> {
                    List<User> users = new ArrayList<>();
                    result.forEach(user -> users.add(new User(user)));
                    return Single.just(users);
                });
    }

    public Maybe<User>insert(User user){
        return mongoClient.rxInsert(COLLECTION_NAME, JsonObject.mapFrom(user))
                .flatMap(result -> {
                    JsonObject jsonObject = new JsonObject().put("_id", result);
                    User insertedUser = new User(jsonObject);
                    return Maybe.just(insertedUser);
                });
    }

    public Maybe<User> getById(String id){
        JsonObject query = new JsonObject().put("_id", id);
        return mongoClient.rxFindOne(COLLECTION_NAME, query, null)
                .flatMap(result -> {
                    User user = new User(result);
                    return Maybe.just(user);
                });
    }

    public Completable update(String id, User user){
        JsonObject query = new JsonObject().put("_id", id);
        return mongoClient.rxReplaceDocuments(COLLECTION_NAME, query, JsonObject.mapFrom(user))
                .flatMapCompletable(result -> {
                    if(result.getDocModified() == 1){
                        return Completable.complete();
                    }else{
                        return Completable.error(new NoSuchElementException("No user with id: "+id));
                    }
                });
    }

    public Completable delete(String id){
        JsonObject query = new JsonObject().put("_id", id);
        return mongoClient.rxRemoveDocument(COLLECTION_NAME, query)
                .flatMapCompletable(result -> {
                    if(result.getRemovedCount() == 1){
                        return Completable.complete();
                    }else{
                        return Completable.error(new NoSuchElementException("No user with id: "+id));
                    }
                });
    }

    public Handler<Message<Object>> getAllUser() {
        logger.debug("Start get all user by event bus...");
        return handler -> mongoClient.rxFind(COLLECTION_NAME, new JsonObject())
                    .subscribe(jsonObjects -> {
                        handler.rxReply(new JsonArray(jsonObjects).encodePrettily()).subscribe();
                    },error -> {
                        handler.fail(500, error.getMessage());
                    });
    }

    public Handler<Message<JsonObject>> insertUser() {
        logger.debug("Start create user by event bus...");
        return handler -> {
            JsonObject newUser = handler.body();
            JsonObject query = new JsonObject().put("username", newUser.getString("username"));
            mongoClient.rxFindOne(COLLECTION_NAME, query, null)
                    .doOnSuccess(entries -> {
                        if(entries != null){
                            handler.fail(404, "user already exists");
                        }
                    }).doOnComplete(() -> {
                        mongoClient.rxInsert(COLLECTION_NAME, newUser).doOnSuccess(insertedUser -> {
                            newUser.put("_id", insertedUser);
                            handler.rxReply(newUser.encode()).subscribe();
                        }).doOnError(error -> handler.fail(500, error.getMessage()))
                                .subscribe();
                    })
                    .subscribe();
        };
    }

    public Handler<Message<Object>> getUserById() {
        return handler -> {
            final Object body = handler.body();
            final String id = body.toString();
            mongoClient.rxFindOne(COLLECTION_NAME, new JsonObject().put("_id", id), null)
                    .doOnSuccess(user -> {
                        handler.rxReply(user.encode()).subscribe();
                    })
                    .doOnComplete(() -> handler.fail(404, "user with id: "+id + " not exists"))
                    .subscribe();
        };
    }

    public Handler<Message<JsonObject>> updateUser() {
        return handler -> {
            JsonObject user = handler.body();
            JsonObject query = new JsonObject().put("_id", user.getString("_id"));
            mongoClient.rxFindOne(COLLECTION_NAME, query, null)
                    .doOnSuccess(entries -> {
                        mongoClient.rxReplaceDocuments(COLLECTION_NAME, query, user).
                                doOnSuccess(result -> {
                                    handler.rxReply(user.encode()).subscribe();
                                }).doOnError(error ->  handler.fail(500, error.getMessage()))
                                .subscribe();
                    }).doOnComplete(() -> handler.fail(404, "user does not exists"))
                    .subscribe();
        };
    }

    public Handler<Message<String>> deleteUser(){
        return handler -> {
            String id = handler.body();
            JsonObject query = new JsonObject().put("_id", id);
            mongoClient.rxFindOne(COLLECTION_NAME, query, null)
                    .doOnSuccess(entries -> {
                        mongoClient.rxRemoveDocument(COLLECTION_NAME, query).
                                doOnSuccess(result -> {
                                    handler.rxReply("delete success").subscribe();
                                }).doOnError(error ->  handler.fail(500, error.getMessage()))
                                .subscribe();
                    }).doOnComplete(() -> handler.fail(404, "user with id: "+id + " not exists"))
                    .subscribe();
        };
    }
}
