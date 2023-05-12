package com.example.demo.repository;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

public class UserVersionRepository {

    private final String COLLECTION_NAME = "version";

    private final MongoClient mongoClient;

    public UserVersionRepository(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }


    public Single<List<JsonObject>> getAll(){
        return Single.create(singleEmitter -> {
           mongoClient.find(COLLECTION_NAME, new JsonObject(), result -> {
               if (result.succeeded()){
                   singleEmitter.onSuccess(result.result());
               }else{
                   singleEmitter.onError(result.cause());
               }
           }) ;
        });
    }

    public Single<JsonObject> insert(JsonObject body){
        return Single.create(singleEmitter -> {
            mongoClient.insert(COLLECTION_NAME, body, result -> {
                if(result.succeeded()){
                    body.put("_id", result.result());
                    singleEmitter.onSuccess(body);
                }else{
                    singleEmitter.onError(result.cause());
                }
            });
        });
    }
}
