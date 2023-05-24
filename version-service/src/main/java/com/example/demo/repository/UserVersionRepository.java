package com.example.demo.repository;

import com.example.demo.exception.CustomException;
import io.reactivex.Completable;
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

    public Single<List<JsonObject>> getByUserId(String userId){
        return Single.create(singleEmitter -> {
            JsonObject query  = new JsonObject().put("userId", userId);
            mongoClient.find(COLLECTION_NAME, query, result -> {
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

    public Completable delete(String userId){
        return Completable.create(completableEmitter -> {
           JsonObject query = new JsonObject().put("userId", userId);
           mongoClient.find(COLLECTION_NAME, query, findResult -> {
              if(findResult.succeeded()){
                  if(findResult.result().size() >= 1){
                      mongoClient.removeDocuments(COLLECTION_NAME, query, deleteResult -> {
                          if(deleteResult.succeeded()){
                              completableEmitter.onComplete();
                          }else{
                              completableEmitter.onError(new CustomException("An error occur when delete user versions"));
                          }
                      });
                  }else{
                      completableEmitter.onError(new CustomException("there is no version records with user id: "+userId));
                  }
              }else{
                  completableEmitter.onError(new CustomException("An error occur when get all version with user id: "+userId));
              }
           });
        });
    }
}
