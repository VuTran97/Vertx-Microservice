package org.example.repository;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.reactivex.SingleHelper;
import org.example.verticle.UserVerticle;

import java.util.List;

public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserVerticle.class);

    private static final String COLLECTION_NAME = "users";

    private final MongoClient mongoClient;

    public UserRepository(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

//    public Handler<Message<Object>> getAllUser() {
//        logger.debug("Start get all user by event bus...");
//        return handler -> {
//            mongoClient.find(COLLECTION_NAME, new JsonObject(), result -> {
//                if(result.succeeded()){
//                    handler.reply(new JsonArray(result.result()).encodePrettily());
//                }else{
//                    handler.fail(500, "lookup failed");
//                }
//            });
//        };
//    }

    public Future<List<JsonObject>> getAll(){
        Future<List<JsonObject>> future = Future.future();
        mongoClient.find(COLLECTION_NAME, new JsonObject(), result -> {
            if(result.succeeded()){
                future.complete(result.result());
            }else{
                future.fail(result.cause());
            }
        });
        return future;

    }


//    public Handler<Message<JsonObject>> insertUser() {
//        logger.debug("Start create user by event bus...");
//        return handler -> {
//            JsonObject body = handler.body();
//            JsonObject query = new JsonObject().put("username", body.getString("username"));
//            mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
//                if(result.failed()){
//                    handler.fail(500, "lookup failed");
//                    return;
//                }
//                JsonObject user = result.result();
//                if(user != null){
//                    handler.fail(404, "user already exists");
//                }else{
//                    mongoClient.insert(COLLECTION_NAME, body, insert -> {
//                        if(insert.failed()){
//                            handler.fail(500, "lookup failed");
//                            return;
//                        }
//                        body.put("_id", insert.result());
//                        handler.reply(body.encode());
//                    });
//                }
//            });
//        };
//    }

    public Future<JsonObject> insert(JsonObject body){
        Future<JsonObject> future = Future.future();
        JsonObject query = new JsonObject().put("username", body.getString("username"));
        mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
            if(result.failed()){
                future.fail("An error occur when get user: "+body.getString("username"));
                return;
            }
            JsonObject user = result.result();
            if(user != null){
                future.fail("user already exists");
            }else{
                mongoClient.insert(COLLECTION_NAME, body, insert -> {
                    if(insert.failed()){
                        future.fail("An error occur when insert user");
                        return;
                    }
                    body.put("_id", insert.result());
                    future.complete(body);
                });
            }
        });
        return future;
    }

//    public Handler<Message<String>> getUserById() {
//        return handler -> {
//            String id = handler.body();
//            JsonObject query = new JsonObject().put("_id", id);
//            mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
//                if(result.failed()){
//                    handler.fail(500, "lookup failed");
//                    return;
//                }
//                JsonObject user = result.result();
//                if(user == null){
//                    handler.fail(404, "user with id: "+id+ " not exists");
//                }else{
//                    handler.reply(user.encode());
//                }
//            });
//        };
//    }

    public Future<JsonObject> getById(String id){
        Future<JsonObject> future = Future.future();
        JsonObject query = new JsonObject().put("_id", id);
        mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
            if(result.failed()){
                future.fail("An error occur when get user with id: "+id);
                return;
            }
            JsonObject user = result.result();
            if(user == null){
                future.fail("user with id: "+id+ " not exists");
            }else{
                future.complete(user);
            }
        });
        return future;
    }

//    public Handler<Message<JsonObject>> updateUser() {
//        return handler -> {
//            JsonObject body = handler.body();
//            JsonObject query = new JsonObject().put("_id", body.getString("_id"));
//            mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
//                if(result.failed()){
//                    handler.fail(500, "lookup failed");
//                    return;
//                }
//                JsonObject user = result.result();
//                if(user == null){
//                    handler.fail(404, "user with id: "+body.getString("_id")+ " not exists");
//                }
//                mongoClient.replaceDocuments(COLLECTION_NAME, query, body, resultUpdate -> {
//                   if(result.failed()){
//                       handler.fail(500, "update failed");
//                   }
//                   handler.reply(body.encode());
//                });
//            });
//        };
//    }

    public Future<JsonObject> update(JsonObject body){
        Future<JsonObject> future = Future.future();
        JsonObject query = new JsonObject().put("_id", body.getString("_id"));
        mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
            if(result.failed()){
                future.fail("An error occur when get user with id: "+body.getString("_id"));
                return;
            }
            JsonObject user = result.result();
            if(user == null){
                future.fail( "user with id: "+body.getString("_id")+ " not exists");
            }
            mongoClient.replaceDocuments(COLLECTION_NAME, query, body, resultUpdate -> {
                if(result.failed()){
                    future.fail("An error occur when update user with id: "+body.getString("_id"));
                }
                future.complete(body);
            });
        });
        return future;
    }

//    public Handler<Message<String>> deleteUser(){
//        return handler -> {
//            String id = handler.body();
//            JsonObject query = new JsonObject().put("_id", id);
//            mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
//                if(result.failed()){
//                    handler.fail(500, "lookup failed");
//                    return;
//                }
//                JsonObject user = result.result();
//                if(user == null){
//                    handler.fail(404, "user with id: "+id+ " not exists");
//                }
//                mongoClient.removeDocument(COLLECTION_NAME, query, resultDelete -> {
//                    if(result.failed()){
//                        handler.fail(500, "delete failed");
//                    }
//                    handler.reply("delete success");
//                });
//            });
//        };
//    }

    public Future<String> delete(String id){
        Future<String> future = Future.future();
        JsonObject query = new JsonObject().put("_id", id);
        mongoClient.findOne(COLLECTION_NAME, query, null, result -> {
            if(result.failed()){
                future.fail("An error occur when get user with id: "+id);
                return;
            }
            JsonObject user = result.result();
            if(user == null){
                future.fail("user with id: "+id+ " not exists");
            }
            mongoClient.removeDocument(COLLECTION_NAME, query, resultDelete -> {
                if(result.failed()){
                    future.fail("An error occur when delete user");
                }
                future.complete("delete success");
            });
        });
        return future;
    }
}
