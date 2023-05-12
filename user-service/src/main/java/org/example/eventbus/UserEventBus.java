package org.example.eventbus;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.example.repository.UserRepository;


public class UserEventBus {

    private UserRepository userRepository;

    public UserEventBus(UserRepository userRepository){
        this.userRepository = userRepository;
    }

        public Handler<Message<Object>> getAll(){
            return handler -> {
                userRepository.getAll().subscribe(jsonObjects -> {
                    handler.reply(new JsonArray(jsonObjects).encodePrettily());
                }, error -> {
                    handler.fail(400, error.getMessage());
                });
            };
        }

    public Handler<Message<JsonObject>> insert(){
        return handler -> {
            JsonObject body = handler.body();
            userRepository.insert(body).subscribe(entries -> {
                handler.reply(entries.encodePrettily());
            }, error -> {
                handler.fail(400, error.getMessage());
            });
        };
    }


    public Handler<Message<String>> getById(){
        return handler -> {
            String id = handler.body();
            userRepository.getById(id).subscribe(item -> {
                handler.reply(item.encodePrettily());
            }, error -> {
                if(error.getMessage().equalsIgnoreCase("user with id: " + id + " not exists")){
                    handler.fail(404, error.getMessage());
                }else{
                    handler.fail(400, error.getMessage());
                }
            });
        };
    }

    public Handler<Message<JsonObject>> update(){
        return handler -> {
            JsonObject body = handler.body();
            userRepository.update(body).subscribe(item -> {
                handler.reply(item.encodePrettily());
            }, error -> {
                if(error.getMessage().equalsIgnoreCase("user with id: "+body.getString("_id")+ " not exists")){
                    handler.fail(404, error.getMessage());
                }else{
                    handler.fail(400, error.getMessage());
                }
            });
        };
    }

    public Handler<Message<String>> delete(){
        return handler -> {
            String id = handler.body();
            userRepository.delete(id).subscribe(() -> {
                handler.reply("delete success");
            }, error -> {
                if(error.getMessage().equalsIgnoreCase("user with id: "+id+ " not exists")){
                    handler.fail(404, error.getMessage());
                }else{
                    handler.fail(400, error.getMessage());
                }
            });
        };
    }

}
