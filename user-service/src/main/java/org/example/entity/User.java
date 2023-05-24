package org.example.entity;

import com.example.demo.entity.UserVersion;
import io.vertx.core.json.JsonObject;
import org.example.converter.UserConverter;

import java.util.List;

public class User {

  private String _id;

  private String username;

  private String password;

  private List<UserVersion> versions;

    public User(){

    }
  public User(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public User(JsonObject json) {
    UserConverter.fromJson(json, this);
  }
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getId() {
    return _id;
  }

  public void setId(String _id) {
    this._id = _id;
  }

  public List<UserVersion> getVersions() {
    return versions;
  }

  public void setVersions(List<UserVersion> versions) {
    this.versions = versions;
  }
}
