package org.example.eventbus;

import com.example.demo.enums.EventAddress;
import com.example.demo.repository.UserVersionRepository;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;


public class UserEventBus {

    private UserRepository userRepository;

    private UserVersionRepository userVersionRepository;

    public UserEventBus(UserRepository userRepository, UserVersionRepository userVersionRepository) {
        this.userRepository = userRepository;
        this.userVersionRepository = userVersionRepository;
    }

    /**
     * this lamda function return type is message
     * @param vertx
     * @return
     */
    public Handler<Message<Object>> getAll(Vertx vertx) {
        return handler -> {
            userRepository.getAll().flatMap(jsonObjects -> Observable.fromIterable(jsonObjects)
                    .map(item -> getVersion(vertx, item))
                    .toList()
                    .flatMap(singles -> Observable.fromIterable(singles)
                            .flatMap(single -> single.toObservable())
                            .toList()
                            .doOnSuccess(result -> handler.reply(new JsonArray(result).encodePrettily()))
                            .doOnError(error -> handler.reply(error))
                          )).subscribe();
        };
    }

    public Single<List<JsonObject>> getAllEB(){
        return Single.create(singleEmitter -> {
           userRepository.getAll().flatMap(jsonObjects -> Observable.fromIterable(jsonObjects)
                   .map(user -> userVersionRepository.getUserWithVersion(user))
                   .flatMap(single -> single.toObservable())
                           .toList()
           ).subscribe(result -> {
               singleEmitter.onSuccess(result);
           }, error -> {
               singleEmitter.onError(error);
           });

        });
    }


    private Single<JsonObject> getVersion(Vertx vertx, JsonObject user) {
        return Single.create(singleEmitter -> {
            vertx.eventBus().send(EventAddress.GET_USER_VERSION_BY_USER_ID.name(), user.getString("_id"), messageAsyncResult -> {
                if (messageAsyncResult.succeeded()) {
                    JsonArray versions = new JsonArray(Json.encode(messageAsyncResult.result().body()));
                    user.put("versions", versions);
                    singleEmitter.onSuccess(user);
                } else {
                    singleEmitter.onError(messageAsyncResult.cause());
                }
            });
        });
    }

    public Handler<Message<JsonObject>> insert(Vertx vertx) {
        return handler -> {
            JsonObject userBody = handler.body();
            userRepository.insert(userBody).subscribe(entries -> {
                JsonObject versionBody = new JsonObject().put("version", entries.getDouble("version"))
                        .put("userId", entries.getString("_id"));
                vertx.eventBus().send(EventAddress.INSERT_USER_VERSION.name(), versionBody, messageAsyncResult -> {
                    if (messageAsyncResult.succeeded()) {
                        handler.reply(entries.encodePrettily());
                    } else {
                        handler.fail(400, messageAsyncResult.cause().getMessage());
                    }
                });
            }, error -> {
                handler.fail(400, error.getMessage());
            });
        };
    }


    public Handler<Message<String>> getById(Vertx vertx) {
        return handler -> {
            String id = handler.body();
            vertx.eventBus().send(EventAddress.GET_USER_VERSION_BY_USER_ID.name(), id, messageAsyncResult -> {
                if (messageAsyncResult.succeeded()) {
                    JsonArray versions = new JsonArray(Json.encode(messageAsyncResult.result().body()));
                    userRepository.getById(id).subscribe(user -> {
                        user.put("version", versions);
                        handler.reply(user.encodePrettily());
                    }, error -> {
                        if (error.getMessage().equalsIgnoreCase("user with id: " + id + " not exists")) {
                            handler.fail(404, error.getMessage());
                        } else {
                            handler.fail(400, error.getMessage());
                        }
                    });
                } else {
                    handler.fail(400, messageAsyncResult.cause().getMessage());
                }
            });

        };
    }

    public Handler<Message<JsonObject>> update(Vertx vertx) {
        return handler -> {
            JsonObject userBody = handler.body();
            //get all version of user and check if version is already exists, then not update
            vertx.eventBus().send(EventAddress.GET_USER_VERSION_BY_USER_ID.name(), userBody.getString("_id"), messageAsyncResult -> {
                if (messageAsyncResult.succeeded()) {
                    JsonArray versions = new JsonArray(Json.encode(messageAsyncResult.result().body()));
                    List<Double> versionValues = versions.stream().map(jsonValue -> ((JsonObject) jsonValue).getDouble("version"))
                            .collect(Collectors.toList());
                    if (versionValues.contains(userBody.getDouble("version"))) {
                        handler.fail(400, "the version: " + userBody.getDouble("version") + " of user with id: " + userBody.getString("_id") + " is already exists");
                    } else {
                        userRepository.update(userBody).subscribe(item -> {
                            JsonObject versionBody = new JsonObject().put("version", item.getDouble("version"))
                                    .put("userId", item.getString("_id"));
                            vertx.eventBus().send(EventAddress.INSERT_USER_VERSION.name(), versionBody, result -> {
                                if (result.succeeded()) {
                                    handler.reply(item.encodePrettily());
                                } else {
                                    handler.fail(400, result.cause().getMessage());
                                }
                            });
                        }, error -> {
                            if (error.getMessage().equalsIgnoreCase("user with id: " + userBody.getString("_id") + " not exists")) {
                                handler.fail(404, error.getMessage());
                            } else {
                                handler.fail(400, error.getMessage());
                            }
                        });
                    }
                } else {
                    handler.fail(400, messageAsyncResult.cause().getMessage());
                }
            });
        };
    }

    public Handler<Message<String>> delete(Vertx vertx) {
        return handler -> {
            String id = handler.body();
            userRepository.delete(id).subscribe(() -> {
                vertx.eventBus().send(EventAddress.DELETE_VERSION_BY_USER_ID.name(), id, messageAsyncResult -> {
                    if (messageAsyncResult.succeeded()) {
                        handler.reply("delete success");
                    } else {
                        handler.fail(400, messageAsyncResult.cause().getMessage());
                    }
                });
            }, error -> {
                if (error.getMessage().equalsIgnoreCase("user with id: " + id + " not exists")) {
                    handler.fail(404, error.getMessage());
                } else {
                    handler.fail(400, error.getMessage());
                }
            });
        };
    }

}
