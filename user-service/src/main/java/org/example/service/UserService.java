package org.example.service;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.example.entity.User;

import java.util.List;

public interface UserService {
    Maybe<User> insert(User user);

    Single<List<User>> getAll();

    Maybe<User> getById(String id);

    Completable update(String id, User user);

    Completable delete(String id);
}
