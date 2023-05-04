package org.example.converter;

import io.vertx.core.json.JsonObject;
import org.example.entity.User;

public class UserConverter {
  public static JsonObject toJson(User obj, JsonObject json){
    if (obj.getUsername() != null) {
      json.put("_id", obj.getId());
    }
    if (obj.getUsername() != null) {
      json.put("username", obj.getUsername());
    }
    if (obj.getPassword() != null) {
      json.put("password", obj.getPassword());
    }
    return json;
  }

  public static JsonObject fromJson(JsonObject json,User obj){
    if (json.getValue("_id") instanceof String) {
      obj.setId((String)json.getValue("_id"));
    }
    if (json.getValue("username") instanceof String) {
      obj.setUsername((String)json.getValue("username"));
    }
    if (json.getValue("password") instanceof String) {
      obj.setPassword((String)json.getValue("password"));
    }
    return json;
  }
}
