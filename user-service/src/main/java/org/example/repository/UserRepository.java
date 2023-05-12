package org.example.repository;

import com.example.demo.exception.CustomException;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.example.verticle.UserVerticle;

import java.util.List;

public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

    private static final String COLLECTION_NAME = "users";

    private final MongoClient mongoClient;

    public UserRepository(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public Single<List<JsonObject>> getAll(){
        return Single.create(singleEmitter -> {
            mongoClient.find(COLLECTION_NAME, new JsonObject(), result -> {
                if(result.succeeded()){
                    singleEmitter.onSuccess(result.result());
                }else{
                    singleEmitter.onError(result.cause());
                }
            });
        });
    }


    public Single<JsonObject> insert(JsonObject body){
        return Single.create(singleEmitter -> {
            JsonObject query = new JsonObject().put("username", body.getString("username"));
            mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
                if(result.failed()){
                    singleEmitter.onError(new CustomException("An error occur when get user: "+body.getString("username")));
                    return;
                }
                JsonObject user = result.result();
                if(user != null){
                    singleEmitter.onError(new CustomException("user already exists"));
                }else{
                    mongoClient.insert(COLLECTION_NAME, body, insert -> {
                        if(insert.failed()){
                            singleEmitter.onError(new CustomException("An error occur when insert user"));
                            return;
                        }
                        body.put("_id", insert.result());
                        singleEmitter.onSuccess(body);
                    });
                }
            });
        });
    }

    public Single<JsonObject> getById(String id) {
        return Single.create(singleEmitter -> {
            JsonObject query = new JsonObject().put("_id", id);
            mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
                if (result.failed()) {
                    singleEmitter.onError(new CustomException("An error occur when get user with id: " + id));
                    return;
                }
                JsonObject user = result.result();
                if (user == null) {
                    singleEmitter.onError(new CustomException("user with id: " + id + " not exists"));
                } else {
                    singleEmitter.onSuccess(user);
                }
            });
        });
    }

    public Single<JsonObject> update(JsonObject body){
        return Single.create(singleEmitter -> {
            JsonObject query = new JsonObject().put("_id", body.getString("_id"));
            mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
                if(result.failed()){
                    singleEmitter.onError(new CustomException("An error occur when get user with id: "+body.getString("_id")));
                    return;
                }
                JsonObject user = result.result();
                if(user == null){
                    singleEmitter.onError(new CustomException("user with id: "+body.getString("_id")+ " not exists"));
                }
                mongoClient.replaceDocuments(COLLECTION_NAME, query, body, resultUpdate -> {
                    if(result.failed()){
                        singleEmitter.onError(new CustomException("An error occur when update user with id: "+body.getString("_id")));
                    }
                    singleEmitter.onSuccess(body);
                });
            });
        });
    }

    public Completable delete(String id){
        return Completable.create(completableEmitter -> {
            JsonObject query = new JsonObject().put("_id", id);
            mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
                if(result.failed()){
                    completableEmitter.onError(new CustomException("An error occur when get user with id: "+id));
                    return;
                }
                JsonObject user = result.result();
                if(user == null){
                    completableEmitter.onError(new CustomException("user with id: "+id+ " not exists"));
                }
                mongoClient.removeDocument(COLLECTION_NAME, query, resultDelete -> {
                    if(result.failed()){
                        completableEmitter.onError(new CustomException("An error occur when delete user"));
                    }
                    completableEmitter.onComplete();
                });
            });
        });
    }
}
