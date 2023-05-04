package org.example.repository;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.example.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class UserRepository {

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

//    public Handler<Message<Object>> getAllUser() {
//        return handler -> mongoClient.find("users", new JsonObject(), lookup -> {
//            // error handling
//            if (lookup.failed()) {
//                handler.fail(500, "lookup failed");
//                return;
//            }
//            handler.rxReply(new JsonArray(lookup.result()).encode()).subscribe();
//        });
//
//    }
}
