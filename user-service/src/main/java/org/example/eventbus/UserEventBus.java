package org.example.eventbus;

import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.SingleHelper;
import org.example.repository.UserRepository;


public class UserEventBus {

    private UserRepository userRepository;

    public UserEventBus(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Handler<Message<Object>> getAll(){
        return handler -> {
          userRepository.getAll().setHandler(listAsyncResult -> {
              if(listAsyncResult.succeeded()){
                  handler.reply(new JsonArray(listAsyncResult.result()).encodePrettily());
              }else{
                  handler.fail(400, listAsyncResult.cause().getMessage());
              }
          });
        };

//        return handler -> {
//            Single<Object> single = SingleHelper.toSingle(asyncResultHandler -> {
//                userRepository.getAll().setHandler(listAsyncResult -> {
//                    if(listAsyncResult.succeeded()){
//                        handler.reply(new JsonArray(listAsyncResult.result()).encodePrettily());
//                    }else{
//                        handler.fail(400, listAsyncResult.cause().getMessage());
//                    }
//                });
//            });
//            single.subscribe();
//        };
    }

    public Handler<Message<JsonObject>> insert(){
        return handler -> {
            JsonObject body = handler.body();
            userRepository.insert(body).setHandler(jsonObjectAsyncResult -> {
                if(jsonObjectAsyncResult.succeeded()){
                    handler.reply(jsonObjectAsyncResult.result().encodePrettily());
                }else{
                    handler.fail(400, jsonObjectAsyncResult.cause().getMessage());
                }
            });
        };
    }

    public Handler<Message<String>> getById(){
        return handler -> {
            String id = handler.body();
            userRepository.getById(id).setHandler(jsonObjectAsyncResult -> {
                if(jsonObjectAsyncResult.succeeded()){
                    handler.reply(jsonObjectAsyncResult.result().encodePrettily());
                }else{
                    handler.fail(400, jsonObjectAsyncResult.cause().getMessage());
                }
            });
        };
    }

    public Handler<Message<JsonObject>> update(){
        return handler -> {
            JsonObject body = handler.body();
            userRepository.update(body).setHandler(jsonObjectAsyncResult -> {
                if(jsonObjectAsyncResult.succeeded()){
                    handler.reply(jsonObjectAsyncResult.result().encodePrettily());
                }else{
                    handler.fail(400, jsonObjectAsyncResult.cause().getMessage());
                }
            });
        };
    }

    public Handler<Message<String>> delete(){
        return handler -> {
            String id = handler.body();
            userRepository.delete(id).setHandler(jsonObjectAsyncResult -> {
                if(jsonObjectAsyncResult.succeeded()){
                    handler.reply(jsonObjectAsyncResult.result());
                }else{
                    handler.fail(400, jsonObjectAsyncResult.cause().getMessage());
                }
            });
        };
    }

}
